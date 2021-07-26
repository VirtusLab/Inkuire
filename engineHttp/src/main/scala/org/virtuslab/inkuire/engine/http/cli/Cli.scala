package org.virtuslab.inkuire.engine.http.cli

import java.io.File
import java.net.URL
import java.nio.file.Paths

import cats.Id
import cats.data.EitherT
import cats.data.StateT
import cats.instances.all._
import cats.syntax.all._
import cats.effect.IO
import org.virtuslab.inkuire.engine.common.api.OutputHandler
import org.virtuslab.inkuire.engine.common.api.{ConfigReader, InputHandler, OutputHandler}
import org.virtuslab.inkuire.engine.common.model.{AppConfig, InkuireDb}
import org.virtuslab.inkuire.engine.common.model.Engine.Env
import org.virtuslab.inkuire.engine.common.utils.helpers.IOHelpers
import org.virtuslab.inkuire.engine.common.utils.syntax._
import org.virtuslab.inkuire.engine.common.model.InkuireDb
import org.virtuslab.inkuire.engine.common.model.Engine._

import scala.io.StdIn.readLine
import scala.annotation.tailrec
import scala.io.Source
import scala.util.chaining._

import org.virtuslab.inkuire.engine.common.serialization.EngineModelSerializers
import cats.kernel.Monoid

class Cli extends InputHandler with OutputHandler with ConfigReader with IOHelpers {

  @tailrec
  private def parseArgs[P](f: (String, String) => AppConfig)(
    args:                     List[String],
    agg:                      Either[String, List[AppConfig]] = Right(List.empty)
  ): Either[String, List[AppConfig]] = {
    args match {
      case Nil => agg
      case opt :: v :: tail =>
        parseArgs(f)(
          tail,
          agg >>= { list =>
            f(opt, v)
              .right[String]
              .fmap(list :+ _)
          }
        )
      case arg :: Nil =>
        s"Wrong argument $arg".left
    }
  }

  private def handleSyntaxError(err: String): Engine[Unit] = {
    IO {
      println("Syntax error:")
      println(err)
    }.liftApp
  }

  private def handleResolveError(err: String): Engine[Unit] = {
    IO {
      println("Resolve error:")
      println(err)
    }.liftApp
  }

  private def handleCommand(input: String): Engine[Unit] = {
    StateT.get[IO, Env] >>= { env =>
      env.parser
        .parse(input)
        .fold(
          handleSyntaxError,
          s => {
            env.resolver
              .resolve(s)
              .fold(
                handleResolveError,
                r => putStrLn(env.prettifier.prettify(env.matcher findMatches r)).liftApp
              )
          }
        )
    }
  }

  override def serveOutput(): Engine[Unit] = {
    IO {
      print(s"inkuire> ")
      readLine()
    }.liftApp >>= { command: String =>
      if (command.toLowerCase == "exit") {
        IO { println("bye") }.liftApp
      } else {
        handleCommand(command) >>
          serveOutput
      }
    }
  }

  private def getURLs(url: URL, filesExtension: String): List[URL] = {
    if (url.toURI.getScheme.toLowerCase == "file" && new File(url.toURI).isDirectory)
      new File(url.toURI).listFiles(_.getName.endsWith(filesExtension)).map(_.toURI.toURL).toList
    else List(url)
  }

  private def getURLContent(url: URL) = Source.fromInputStream(url.openStream()).getLines().mkString

  override def readInput(appConfig: AppConfig): EitherT[IO, String, InkuireDb] = {
    appConfig.inkuirePaths
      .flatMap(path => getURLs(new URL(path), ".json"))
      .map(getURLContent)
      .toList
      .traverse(f => IO(f))
      .map { contents =>
        contents
          .map(EngineModelSerializers.deserialize)
          .collect {
            case Right(db) => db
          }
          .pipe(Monoid.combineAll[InkuireDb])
      }
      .pipe(EitherT.right(_))
  }

  override def readConfig(args: Seq[String]): EitherT[IO, String, AppConfig] = {
    parseArgs(AppConfig.parseCliOption)(args.toList)
      .map(_.combineAll)
      .pipe(config => new EitherT(IO(config)))
  }
}
