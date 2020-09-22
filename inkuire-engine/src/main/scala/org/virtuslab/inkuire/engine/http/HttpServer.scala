package org.virtuslab.inkuire.engine.http

import cats.effect._
import cats.implicits._
import com.google.gson.Gson
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.{HttpRoutes, MediaType, UrlForm}
import org.virtuslab.inkuire.engine.api.OutputHandler
import org.virtuslab.inkuire.engine.model.Engine._
import org.virtuslab.inkuire.model.OutputFormat

import scala.concurrent.ExecutionContext.global

object SignatureParameter extends QueryParamDecoderMatcher[String]("signature")

class HttpServer extends OutputHandler {

  override def serveOutput(env: Env): IO[Unit] = {

    implicit val cs:    ContextShift[IO] = IO.contextShift(global)
    implicit val timer: Timer[IO]        = IO.timer(global)

    val formatter = new OutputFormatter(env.prettifier)
    val gson = new Gson()

    def results(signature: String): Either[String, OutputFormat] = {
      env.parser
        .parse(signature)
        .map(
          fb => formatter.createOutput(signature, env.matcher |??| fb)
        )
    }

    val app = HttpRoutes
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
                gson.toJson(fb),
                `Content-Type`(MediaType.application.json)
              )
          )
      }
      .orNotFound

    BlazeServerBuilder[IO]
      .bindHttp(env.appConfig.port.port, env.appConfig.address.address)
      .withHttpApp(app)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
