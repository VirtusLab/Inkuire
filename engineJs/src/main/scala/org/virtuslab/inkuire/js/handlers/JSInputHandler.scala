package org.virtuslab.inkuire.js.handlers

import cats.data.EitherT
import cats.effect.ContextShift
import cats.effect.IO
import cats.instances.all._
import cats.kernel.Monoid
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.parser._
import org.scalajs.dom.ext.Ajax
import org.virtuslab.inkuire.engine.common.api.ConfigReader
import org.virtuslab.inkuire.engine.common.api.InputHandler
import org.virtuslab.inkuire.engine.common.model.AppConfig
import org.virtuslab.inkuire.engine.common.model.InkuireDb
import org.virtuslab.inkuire.engine.common.serialization.EngineModelSerializers

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.chaining._

class JSInputHandler(private val scriptPath: String) extends InputHandler with ConfigReader {

  private def tryGetURLContent(url: String): Future[Either[String, String]] =
    Ajax
      .get(url)
      .map(_.responseText.pipe(Right(_)))
      .fallbackTo(Future(Left("Could not read contents of file")))

  implicit def contextShift(implicit ec: ExecutionContext): ContextShift[IO] = IO.contextShift(ec)

  override def readConfig(args: Seq[String]): EitherT[IO, String, AppConfig] = {
    args.headOption
      .toRight("Missing configuration url")
      .map(tryGetURLContent)
      .flatTraverse(f => IO.fromFuture(IO(f)))
      .map(_.flatMap(parseConfig))
      .pipe(new EitherT(_))
      .leftMap(_ => "Inkuire seems to be disabled. To enable it add `-Ygenerate-inkuire` flag to scaladoc options.")
  }

  override def readInput(appConfig: AppConfig): EitherT[IO, String, InkuireDb] = {
    appConfig.inkuirePaths
      .map(scriptPath + _)
      .map(tryGetURLContent)
      .toList
      .traverse(f => IO.fromFuture(IO(f)))
      .map { contents =>
        contents
          .map(_.flatMap(EngineModelSerializers.deserialize))
          .collect {
            case Right(db) => db
          }
          .pipe(Monoid.combineAll[InkuireDb])
      }
      .pipe(EitherT.right(_))
  }

  private def parseConfig(config: String): Either[String, AppConfig] = {
    parse(config)
      .flatMap(_.as[AppConfig])
      .leftMap(_.show)
  }
}
