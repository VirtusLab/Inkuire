package org.virtuslab.inkuire.engine.http.http

import cats.effect._
import cats.implicits._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.dsl.io._
import org.http4s.headers.{`Content-Type`, Location}
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.{HttpRoutes, MediaType, Request, StaticFile, Uri, UrlForm}
import org.virtuslab.inkuire.engine.common.api.OutputHandler
import org.virtuslab.inkuire.engine.common.model.Engine.Env
import org.virtuslab.inkuire.engine.common.model.Engine._
import org.virtuslab.inkuire.engine.common.serialization.EngineModelSerializers
import org.virtuslab.inkuire.engine.http.config.HttpAppConfig
import org.virtuslab.inkuire.model.OutputFormat

import scala.concurrent.ExecutionContext.global
import scala.io.Source

object SignatureParameter extends QueryParamDecoderMatcher[String]("signature")

class HttpServer extends OutputHandler {

  override def serveOutput(env: Env): IO[Unit] = {

    implicit val cs:    ContextShift[IO] = IO.contextShift(global)
    implicit val timer: Timer[IO]        = IO.timer(global)

    val formatter = new OutputFormatter(env.prettifier)
    val appConfig = HttpAppConfig.validate(env.appConfig)

    def results(signature: String): Either[String, OutputFormat] = {
      env.parser
        .parse(signature)
        .map(env.resolver.resolve)
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

    appConfig.fold(
      s => IO.pure(println(s)),
      config => {
        val app = for {
          blocker <- Blocker[IO]
          server <-
            BlazeServerBuilder[IO]
              .bindHttp(config.port.port, config.address.address)
              .withHttpApp(appService(blocker))
              .resource
        } yield server

        app.use(_ => IO.never).as(ExitCode.Success)
      }
    )
  }
}
