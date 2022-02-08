package org.virtuslab.inkuire.http

import org.virtuslab.inkuire.engine.api.Env
import org.virtuslab.inkuire.engine.api.FutureExcept
import org.virtuslab.inkuire.engine.api.InkuireDb
import org.virtuslab.inkuire.engine.api.InputHandler
import org.virtuslab.inkuire.engine.api.OutputHandler
import org.virtuslab.inkuire.engine.impl.service.EngineModelSerializers
import org.virtuslab.inkuire.engine.impl.utils.Monoid

import java.io.File
import java.net.URL
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.io.Source
import scala.io.StdIn.readLine
import scala.util.chaining._

class Cli extends InputHandler with OutputHandler {

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

  override def readInput(args: Seq[String])(implicit ec: ExecutionContext): FutureExcept[InkuireDb] = {
    readConfig(args)
      .pipe(FutureExcept.fromExcept)
      .flatMap { appConfig =>
        appConfig.inkuirePaths
          .flatMap(path => getURLs(new URL(path), ".json"))
          .map(getURLContent)
          .toList
          .map(EngineModelSerializers.deserialize)
          .collect {
            case Right(db) => db
          }
          .pipe(InkuireDb.combineAll)
          .pipe(FutureExcept.pure)
      }
  }

  def readConfig(args: Seq[String]): Either[String, AppConfig] = {
    parseArgs(AppConfig.parseCliOption)(args.toList)
      .map(Monoid.combineAll[AppConfig])
  }
}
