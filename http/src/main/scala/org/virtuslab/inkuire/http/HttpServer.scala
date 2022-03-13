package org.virtuslab.inkuire.http

import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.blaze.server._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.server.middleware._
import org.slf4j.{Logger, LoggerFactory}
import org.virtuslab.inkuire.engine.api.Env
import org.virtuslab.inkuire.engine.api.OutputHandler

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._

import cats.effect.unsafe.implicits.global

object SignatureParameter extends QueryParamDecoderMatcher[String]("signature")

class HttpServer(appConfig: AppConfig) extends OutputHandler {

  val logger: Logger = LoggerFactory.getLogger(classOf[HttpServer])

  override def serveOutput(env: Env)(implicit ec: ExecutionContext): Future[Unit] = {

    def static(file: String, request: Request[IO]) =
      StaticFile.fromResource("/" + file, Some(request)).getOrElseF(NotFound())

    def appService: HttpRoutes[IO] =
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
          case req @ GET -> Root / "assets" / path => static(s"assets/$path", req)
        }

    val methodConfig = CORSConfig.default
      .withAnyOrigin(true)
      .withAnyMethod(true)
      .withAllowCredentials(true)
      .withMaxAge(1.day)

    val app =
      BlazeServerBuilder[IO]
        .withExecutionContext(ec)
        .bindHttp(appConfig.getPort, appConfig.getAddress)
        .withHttpApp(CORS(appService.orNotFound, methodConfig))
        .resource

    app.use(_ => IO.never).unsafeToFuture()
  }
}
