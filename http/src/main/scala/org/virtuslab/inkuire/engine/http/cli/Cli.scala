package org.virtuslab.inkuire.engine.http.cli

import cats.data.EitherT
import cats.data.State
import cats.effect.IO
import cats.instances.all._
import cats.kernel.Monoid
import cats.syntax.all._
import org.virtuslab.inkuire.engine.common.api.ConfigReader
import org.virtuslab.inkuire.engine.common.api.InputHandler
import org.virtuslab.inkuire.engine.common.api.OutputHandler
import org.virtuslab.inkuire.engine.common.model.AppConfig
import org.virtuslab.inkuire.engine.common.model.Engine.Env
import org.virtuslab.inkuire.engine.common.model.Engine._
import org.virtuslab.inkuire.engine.common.model.InkuireDb
import org.virtuslab.inkuire.engine.common.serialization.EngineModelSerializers

import java.io.File
import java.net.URL
import scala.annotation.tailrec
import scala.io.Source
import scala.io.StdIn.readLine
import scala.util.chaining._

class Cli extends InputHandler with OutputHandler with ConfigReader {

  @tailrec
  private def parseArgs(
    f: (String, String) => AppConfig
  )(
    args: List[String],
    agg: Either[String, List[AppConfig]] = Right(List.empty)
  ): Either[String, List[AppConfig]] = {
    args match {
      case Nil => agg
      case opt :: v :: tail =>
        parseArgs(f)(
          tail,
          agg.map(_ :+ f(opt, v))
        )
      case arg :: Nil =>
        Left(s"Wrong argument $arg")
    }
  }

  private def handleCommand(input: String): Engine[Unit] =
    State.get[Env].map { env =>
      env.parser
        .parse(input)
        .flatMap { s =>
          env.resolver.resolve(s)
        }
        .map { r =>
          env.matcher.findMatches(r).map { case (fun, _) => fun }
        }
        .fold(
          println,
          matches => println(env.prettifier.prettify(matches))
        )
    }

  override def serveOutput(env: Env): Unit = {
    print(s"inkuire> ")
    val command: String = readLine()
    if (command.toLowerCase == "exit") {
      println("bye")
    } else {
      handleCommand(command)
      serveOutput(env)
    }
  }

  private def getURLs(url: URL, filesExtension: String): List[URL] = {
    if (url.toURI.getScheme.toLowerCase == "file" && new File(url.toURI).isDirectory)
      new File(url.toURI).listFiles(_.getName.endsWith(filesExtension)).map(_.toURI.toURL).toList
    else List(url)
  }

  private def getURLContent(url: URL): String = Source.fromInputStream(url.openStream()).getLines().mkString

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
