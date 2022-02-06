package org.virtuslab.inkuire.engine.http.cli

import org.virtuslab.inkuire.engine.common.api.ConfigReader
import org.virtuslab.inkuire.engine.common.api.FutureExcept
import org.virtuslab.inkuire.engine.common.api.InputHandler
import org.virtuslab.inkuire.engine.common.api.OutputHandler
import org.virtuslab.inkuire.engine.common.model.AppConfig
import org.virtuslab.inkuire.engine.common.model.Env
import org.virtuslab.inkuire.engine.common.model.InkuireDb
import org.virtuslab.inkuire.engine.common.serialization.EngineModelSerializers
import org.virtuslab.inkuire.engine.common.utils.Monoid

import java.io.File
import java.net.URL
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.io.Source
import scala.io.StdIn.readLine
import scala.util.chaining._

class Cli extends InputHandler with OutputHandler with ConfigReader {

  @tailrec
  private def parseArgs(
    f: (String, String) => AppConfig
  )(
    args: List[String],
    agg:  Either[String, List[AppConfig]] = Right(List.empty)
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

  private def handleCommand(env: Env, input: String): Unit =
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

  override def serveOutput(env: Env)(implicit ec: ExecutionContext): Future[Unit] = {
    print(s"inkuire> ")
    val command: String = readLine()
    if (command.toLowerCase == "exit") {
      Future(println("bye"))
    } else {
      Future(handleCommand(env, command))
        .flatMap(_ => serveOutput(env))
    }
  }

  private def getURLs(url: URL, filesExtension: String): List[URL] = {
    if (url.toURI.getScheme.toLowerCase == "file" && new File(url.toURI).isDirectory)
      new File(url.toURI).listFiles(_.getName.endsWith(filesExtension)).map(_.toURI.toURL).toList
    else List(url)
  }

  private def getURLContent(url: URL): String = Source.fromInputStream(url.openStream()).getLines().mkString

  override def readInput(appConfig: AppConfig)(implicit ec: ExecutionContext): FutureExcept[InkuireDb] = {
    appConfig.inkuirePaths
      .flatMap(path => getURLs(new URL(path), ".json"))
      .map(getURLContent)
      .toList
      .map(EngineModelSerializers.deserialize)
      .collect {
        case Right(db) => db
      }
      .pipe(Monoid.combineAll[InkuireDb](_))
      .pipe(FutureExcept.pure)
  }

  override def readConfig(args: Seq[String])(implicit ec: ExecutionContext): FutureExcept[AppConfig] = {
    parseArgs(AppConfig.parseCliOption)(args.toList)
      .map(Monoid.combineAll[AppConfig])
      .pipe(FutureExcept.fromExcept)
  }
}
