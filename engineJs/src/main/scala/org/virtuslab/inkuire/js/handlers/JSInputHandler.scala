package org.virtuslab.inkuire.js.handlers

import cats.data.EitherT
import cats.data.StateT
import cats.instances.all._
import cats.syntax.all._
import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import io.circe.generic.auto._, io.circe.syntax._, io.circe.parser._
import org.scalajs.dom.ext.Ajax
import org.virtuslab.inkuire.engine.common.api.{ConfigReader, InputHandler}
import org.virtuslab.inkuire.engine.common.model.{AppConfig, InkuireDb}
import org.virtuslab.inkuire.engine.common.serialization.EngineModelSerializers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.chaining._
import cats.kernel.Monoid

class JSInputHandler(private val scriptPath: String) extends InputHandler with ConfigReader {
  private def getURLContent(url: String): Future[String] =
    Ajax.get(url).map(_.responseText).fallbackTo(Future(""))

  private def tryGetURLContent(url: String): Future[Either[String, String]] =
    Ajax.get(url)
      .map(_.responseText.pipe(Right(_)))
      .fallbackTo(Future(Left("Inkuire seems to be disabled. To enable it add `-Ygenerate-inkuire` flag to scaladoc options.")))

  implicit def contextShift(implicit ec: ExecutionContext) = IO.contextShift(ec)

  override def readConfig(args: Seq[String]): EitherT[IO, String, AppConfig] = {
    //Assuming we get link to config
    val configLink = args.headOption match {
      case Some(url) => Right(url)
      case None      => Left("Missing configuration link")
    }
    configLink
      .map(tryGetURLContent)
      .flatTraverse(f => IO.fromFuture(IO(f)))
      .map(_.flatMap(parseConfig))
      .pipe(new EitherT(_))
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
