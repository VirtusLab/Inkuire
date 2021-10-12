package org.virtuslab.inkuire.engine.http.http

import cats.effect._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.MediaType
import org.http4s.Request
import org.http4s.StaticFile
import org.http4s.UrlForm
import org.http4s.dsl.io._
import org.http4s.headers.Location
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import org.http4s.server.middleware._
import org.slf4j
import org.slf4j.LoggerFactory
import org.http4s.blaze.server._
import io.circe.syntax._
import io.circe.generic.auto._
import org.virtuslab.inkuire.engine.common.api.OutputHandler
import org.virtuslab.inkuire.engine.common.model.Engine.Env
import org.virtuslab.inkuire.engine.common.model.ResultFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source
import org.slf4j.LoggerFactory
import org.virtuslab.inkuire.engine.common.model.ResultFormat
import org.http4s.server.Router

object SignatureParameter extends QueryParamDecoderMatcher[String]("signature")

class HttpServer extends OutputHandler {

  val logger: slf4j.Logger = LoggerFactory.getLogger(classOf[HttpServer])

  override def serveOutput(env: Env): IO[Unit] = {

    implicit val cs:    ContextShift[IO] = IO.contextShift(global)
    implicit val timer: Timer[IO]        = IO.timer(global)

    val formatter = new OutputFormatter(env.prettifier)

    def results(signature: String): Either[String, ResultFormat] = {
      env.parser
        .parse(signature)
        .left
        .map { e =>
          logger.info("Error when parsing signature: '" + e + "' for signature: " + signature)
          e
        }
        .flatMap { parsed =>
          logger.info(s"Parsed signature: " + signature)
          env.resolver.resolve(parsed)
        }
        .map(env.matcher findMatches _)
        .map(env.matchQualityService sortMatches _)
        .map(formatter.createOutput(signature, _))
    }

    def static(file: String, blocker: Blocker, request: Request[IO]) =
      StaticFile.fromResource("/" + file, blocker, Some(request)).getOrElseF(NotFound())

    def appService(b: Blocker): HttpRoutes[IO] =
      HttpRoutes
        .of[IO] {
          case GET -> Root / "query" =>
            Ok(Templates.formTemplate(), `Content-Type`(MediaType.text.html))
          case req @ POST -> Root / "query" =>
            req.decode[UrlForm] { m =>
              val signature = m.values("query").headOption.get
              val res       = results(signature)
              res.fold(
                fa => BadRequest(fa),
                fb => Ok(Templates.result(fb), `Content-Type`(MediaType.text.html))
              )
            }
          case GET -> Root / "forSignature" :? SignatureParameter(signature) =>
            results(signature).fold(
              fa => BadRequest(fa),
              fb =>
                Ok(
                  fb.asJson.toString,
                  `Content-Type`(MediaType.application.json)
                )
            )
          case GET -> Root =>
            Found(
              Templates.rootPage,
              Location(uri"/query"),
              `Content-Type`(MediaType.text.html)
            )
          case req @ GET -> Root / "assets" / path => static(s"assets/$path", b, req)
        }

    val methodConfig = CORSConfig.default
      .withAnyOrigin(true)
      .withAnyMethod(true)
      .withAllowCredentials(true)
      .withMaxAge(1.day)

    val app = for {
      blocker <- Blocker[IO]
      server <-
        BlazeServerBuilder[IO](global)
          .bindHttp(env.appConfig.getPort, env.appConfig.getAddress)
          .withHttpApp(CORS(appService(blocker).orNotFound, methodConfig))
          .resource
    } yield server

    app.use(_ => IO.never)
  }
}
