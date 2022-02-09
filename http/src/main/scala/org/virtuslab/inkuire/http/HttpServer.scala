package org.virtuslab.inkuire.http

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
import org.http4s.server.blaze._
import org.http4s.server.middleware._
import org.slf4j
import org.slf4j.LoggerFactory
import org.virtuslab.inkuire.engine.api.Env
import org.virtuslab.inkuire.engine.api.OutputHandler

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._

object SignatureParameter extends QueryParamDecoderMatcher[String]("signature")

class HttpServer(appConfig: AppConfig) extends OutputHandler {

  val logger: slf4j.Logger = LoggerFactory.getLogger(classOf[HttpServer])

  override def serveOutput(env: Env)(implicit ec: ExecutionContext): Future[Unit] = {

    implicit val cs:    ContextShift[IO] = IO.contextShift(ec)
    implicit val timer: Timer[IO]        = IO.timer(ec)

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
              val res       = env.run(signature)
              res.fold(
                fa => BadRequest(fa),
                fb => Ok(Templates.result(fb), `Content-Type`(MediaType.text.html))
              )
            }
          case GET -> Root / "forSignature" :? SignatureParameter(signature) =>
            env
              .run(signature)
              .fold(
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
          .bindHttp(appConfig.getPort, appConfig.getAddress)
          .withHttpApp(CORS(appService(blocker), methodConfig))
          .resource
    } yield server

    app.use(_ => IO.never).unsafeToFuture()
  }
}
