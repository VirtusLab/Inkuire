package org.virtuslab.inkuire.js.handlers

import cats.Id
import cats.data.EitherT
import cats.data.StateT
import cats.instances.all._
import cats.syntax.all._
import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser._
import org.scalajs.dom.ext.Ajax
import org.virtuslab.inkuire.engine.common.api.{ConfigReader, InputHandler}
import org.virtuslab.inkuire.engine.common.model.{AppConfig, InkuireDb}
import org.virtuslab.inkuire.engine.common.utils.syntax.AnyInkuireSyntax
import org.virtuslab.inkuire.js.config.JSAppConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class JSInputHandler(private val scriptPath: String) extends InputHandler with ConfigReader {
  private def getURLContent(url: String) = Ajax.get(url).map(_.responseText).fallbackTo(Future("[]"))

  implicit def contextShift(implicit ec: ExecutionContext) = IO.contextShift(ec)

  override def readConfig(args: Seq[String]): EitherT[IO, String, AppConfig] = {
    //Assuming we get link to config
    val configLink = args.headOption match {
      case Some(url) => Right(url)
      case None      => Left("Missing configuration link")
    }
    configLink
      .map(getURLContent)
      .map(_.map(parseConfig))
      .flatTraverse(f => IO.fromFuture(IO(f)))
      .pure[Id]
      .fmap(new EitherT(_))
  }

  def createInkuireDb(appConfig: JSAppConfig): Future[Either[String, InkuireDb]] = {
    val functionSources = appConfig.dbPaths.map(_.path).map(scriptPath + _).map(getURLContent)
    val graphsSources   = appConfig.ancestryGraphPaths.map(_.path).map(scriptPath + _).map(getURLContent)

    for {
      functions <- Future.sequence(functionSources)
      ancestryGraphs <- Future.sequence(graphsSources)
      db <- Future(InkuireDb.read(functions.toList, ancestryGraphs.toList))
    } yield db
  }

  override def readInput(appConfig: AppConfig): EitherT[IO, String, InkuireDb] = {
    val validatedConfig = JSAppConfig.validate(appConfig)
    val db              = validatedConfig.flatTraverse(config => createInkuireDb(config))
    IO.fromFuture(IO(db))
      .pure[Id]
      .fmap(new EitherT(_))
  }

  private def parseConfig(config: String): Either[String, AppConfig] = {
    parse(config)
      .flatMap(_.as[JSAppConfig])
      .leftMap(_.show)
  }
}
