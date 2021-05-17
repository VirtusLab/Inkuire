package org.virtuslab.inkuire.engine.http.http

import cats.effect._
import cats.implicits._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.dsl.io._
import org.http4s.headers.{`Content-Type`, Location}
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.middleware._
import org.http4s.{HttpRoutes, MediaType, Request, StaticFile, Uri, UrlForm}
import org.virtuslab.inkuire.engine.common.api.OutputHandler
import org.virtuslab.inkuire.engine.common.model.Engine.Env
import org.virtuslab.inkuire.engine.common.model.Engine._
import org.virtuslab.inkuire.engine.common.serialization.EngineModelSerializers
import org.virtuslab.inkuire.engine.common.model.OutputFormat
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.global
import scala.io.Source
import org.slf4j.LoggerFactory

object SignatureParameter extends QueryParamDecoderMatcher[String]("signature")

class HttpServer extends OutputHandler {

  val logger = LoggerFactory.getLogger(classOf[HttpServer])

  override def serveOutput(env: Env): IO[Unit] = {

    implicit val cs:    ContextShift[IO] = IO.contextShift(global)
    implicit val timer: Timer[IO]        = IO.timer(global)

    val formatter = new OutputFormatter(env.prettifier)

    def results(signature: String): Either[String, OutputFormat] = {
      env.parser
        .parse(signature)
        .map { parsed =>
          logger.info(s"Got input signature: " + signature)
          env.resolver.resolve(parsed)
        }
        .map(resolved => formatter.createOutput(signature, env.matcher |??| resolved))
    }

    def static(file: String, blocker: Blocker, request: Request[IO]) =
      StaticFile.fromResource("/" + file, blocker, Some(request)).getOrElseF(NotFound())

    def appService(b: Blocker) =
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
        .orNotFound

    val methodConfig = CORSConfig(
      anyOrigin = true,
      anyMethod = true,
      allowCredentials = true,
      maxAge = 1.day.toSeconds
    )

    val app = for {
      blocker <- Blocker[IO]
      server <-
        BlazeServerBuilder[IO]
          .bindHttp(env.appConfig.port.port, env.appConfig.address.address)
          .withHttpApp(CORS(appService(blocker), methodConfig))
          .resource
    } yield server

    app.use(_ => IO.never).as(ExitCode.Success)
  }
}
