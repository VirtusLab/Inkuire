package org.virtuslab.inkuire.engine.http

import cats.effect._
import cats.implicits._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.{HttpRoutes, MediaType, UrlForm}
import org.virtuslab.inkuire.engine.api.OutputHandler
import org.virtuslab.inkuire.engine.model.Engine._

import scala.concurrent.ExecutionContext.global

object SignatureParameter extends QueryParamDecoderMatcher[String]("signature")

class HttpServer extends OutputHandler {

  override def serveOutput(env: Env): IO[Unit] = {

    implicit val cs:    ContextShift[IO] = IO.contextShift(global)
    implicit val timer: Timer[IO]        = IO.timer(global)

    def signatureToResults(signature: String): Either[String, List[String]] = {
      env.parser
        .parse(signature)
        .map(
          fb => env.prettifier.prettify(env.matcher |??| fb).split("\n").toList
        )
    }

    val app = HttpRoutes
      .of[IO] {
        case GET -> Root / "query" =>
          Ok(Templates.formTemplate(), `Content-Type`(MediaType.text.html))
        case req @ POST -> Root / "query" =>
          req.decode[UrlForm] { m =>
            val signature = m.values("query").headOption.get
            val res       = signatureToResults(signature)
            res.fold(
              fa => BadRequest(fa),
              fb => Ok(Templates.result(fb), `Content-Type`(MediaType.text.html))
            )
          }
        case GET -> Root / "forSignature" :? SignatureParameter(signature) =>
          signatureToResults(signature).fold(
            fa => BadRequest(fa),
            fb =>
              Ok(
                fb.mkString("[\"", "\", \"", "\"]"),
                `Content-Type`(MediaType.text.html)
            )
          )
      }
      .orNotFound

    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(app)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
