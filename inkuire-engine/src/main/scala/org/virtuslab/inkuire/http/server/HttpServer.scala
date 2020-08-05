package org.virtuslab.inkuire.http.server

import java.nio.file.Paths

import cats.effect._
import cats.implicits._
import org.http4s.{HttpRoutes, MediaType, UrlForm}
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.dsl.io._
import org.virtuslab.inkuire.engine.cli.service.KotlinExternalSignaturePrettifier
import org.virtuslab.inkuire.engine.model.InkuireDb
import org.virtuslab.inkuire.engine.parser.KotlinSignatureParser
import org.virtuslab.inkuire.engine.service.ExactMatchService
import org.virtuslab.inkuire.http.templates.Templates

object SignatureParameter extends QueryParamDecoderMatcher[String]("signature")

object HttpServer extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    val service: ExactMatchService = new ExactMatchService(loadDatabase(args))
    val prettifier = new KotlinExternalSignaturePrettifier()

    def signatureToResults(signature: String): Either[String, List[String]] = {
      KotlinSignatureParser.parse(signature).map(
        fb => prettifier.prettify(service |??| fb).split("\n").toList
      )
    }

    val app = HttpRoutes.of[IO] {
      case GET -> Root / "query" =>
        Ok(Templates.formTemplate(), `Content-Type`(MediaType.text.html))
      case req @ POST -> Root / "query" =>
        req.decode[UrlForm] { m =>
          val signature = m.values("query").headOption.get
          val res = signatureToResults(signature)
          res.fold(
            fa => BadRequest(fa),
            fb => Ok(Templates.result(fb), `Content-Type`(MediaType.text.html))
          )
        }
      case GET -> Root / "forSignature" :? SignatureParameter(signature) =>
        signatureToResults(signature).fold(
          fa => BadRequest(fa),
          fb => Ok(fb
            .mkString("[\"", "\", \"", "\"]"),
            `Content-Type`(MediaType.text.html)
          )
        )
    }.orNotFound

    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(app)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

  private def loadDatabase(args: List[String]): InkuireDb = {

    val functions = args.sliding(2, 2).collect {
      case "-d" :: tail => Paths.get(tail.head).toFile
    }.toList

    val ancestors = args.sliding(2, 2).collect {
      case "-a" :: tail => Paths.get(tail.head).toFile
    }.toList

    InkuireDb.read(functions, ancestors)
  }
}