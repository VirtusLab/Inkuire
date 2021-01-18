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
import org.virtuslab.inkuire.engine.common.model._
import org.virtuslab.inkuire.engine.common.model.Engine.Env
import org.virtuslab.inkuire.engine.common.utils.helpers.IOHelpers
import org.virtuslab.inkuire.engine.common.utils.syntax._
import org.virtuslab.inkuire.engine.common.model.Engine._
import org.virtuslab.inkuire.engine.http.config._

import scala.io.StdIn.readLine
import scala.annotation.tailrec
import scala.io.Source

class Cli extends InputHandler with OutputHandler with ConfigReader with IOHelpers {

  @tailrec
  private def parseArgs[P](f: (String, String) => Either[String, P])(
    args:                     List[String],
    agg:                      Either[String, List[P]] = Right(List.empty)
  ): Either[String, List[P]] = {
    args match {
      case Nil => agg
      case opt :: v :: tail =>
        parseArgs(f)(
          tail,
          agg >>= { list =>
            f(opt, v)
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
          s => putStrLn(env.prettifier.prettify(env.matcher |??| env.resolver.resolve(s))).liftApp
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
    HttpAppConfig
      .validate(appConfig)
      .flatMap { appConfig =>
        InkuireDb
          .read(
            appConfig.dbPaths.toList.flatMap(path => getURLs(new URL(path.path), ".inkuire.fdb")).map(getURLContent),
            appConfig.ancestryGraphPaths.toList
              .flatMap(path => getURLs(new URL(path.path), ".inkuire.adb"))
              .map(getURLContent)
          )
      }
      .traverse(value =>
        IO {
          value
        }
      )
      .pure[Id]
      .fmap(new EitherT(_))
  }

  override def readConfig(args: Seq[String]): EitherT[IO, String, AppConfig] = {
    parseArgs(parseCliOption)(args.toList)
      .flatMap(HttpAppConfig.create)
      .pure[Id]
      .fmap(config => new EitherT(IO(config)))
  }

  def parseCliOption(opt: String, v: String): Either[String, AppParam] =
    opt match {
      case "-d" | "--database" => DbPath(v).right
      case "-a" | "--ancestry" => AncestryGraphPath(v).right
      case "--address"         => Address(v).right
      case "-p" | "--port"     => Port(v.toInt).right
      case _                   => s"Wrong option $opt".left
    }
}
