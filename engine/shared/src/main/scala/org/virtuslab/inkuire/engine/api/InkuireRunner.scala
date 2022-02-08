package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.impl.model._
import org.virtuslab.inkuire.engine.impl.service._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.chaining._

class InkuireRunner(
  val inputHandler:        InputHandler,
  val outputHandler:       OutputHandler,
  val parser:              BaseSignatureParserService,
  val resolver:            InkuireDb => BaseSignatureResolver,
  val matchService:        InkuireDb => BaseMatchService,
  val matchQualityService: InkuireDb => BaseMatchQualityService,
  val prettifier:          BaseSignaturePrettifier
) {
  def run(args: Seq[String])(implicit ec: ExecutionContext): Future[Unit] =
    inputHandler
      .readInput(args)
      .flatMap { (db: InkuireDb) =>
        outputHandler
          .serveOutput(
            Env(db, matchService(db), matchQualityService(db), prettifier, parser, resolver(db))
          )
          .pipe(FutureExcept.fromFuture)
      }
      .value
      .map(
        _.fold(
          str => println(s"Oooooh man, bad luck. Inkuire encountered an unexpected error. Caused by $str"),
          identity
        )
      )

}

object InkuireRunner {
  def scalaRunner(
    inputHandler:        InputHandler,
    outputHandler:       OutputHandler,
    parser:              BaseSignatureParserService = new ScalaSignatureParserService,
    resolver:            InkuireDb => BaseSignatureResolver = (db: InkuireDb) => new DefaultSignatureResolver(db),
    matchService:        InkuireDb => BaseMatchService = (db: InkuireDb) => new SubstitutionMatchService(db),
    matchQualityService: InkuireDb => BaseMatchQualityService = (db: InkuireDb) => new TopLevelMatchQualityService(db),
    prettifier:          BaseSignaturePrettifier = new ScalaAnnotatedSignaturePrettifier
  ): InkuireRunner =
    new InkuireRunner(
      inputHandler,
      outputHandler,
      parser,
      resolver,
      matchService,
      matchQualityService,
      prettifier
    )
}
