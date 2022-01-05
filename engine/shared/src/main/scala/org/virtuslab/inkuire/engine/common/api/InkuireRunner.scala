package org.virtuslab.inkuire.engine.common.api

import cats.effect.IO
import org.virtuslab.inkuire.engine.common.model._
import org.virtuslab.inkuire.engine.common.parser.ScalaSignatureParserService
import org.virtuslab.inkuire.engine.common.service.DefaultSignatureResolver
import org.virtuslab.inkuire.engine.common.service.ScalaExternalSignaturePrettifier
import org.virtuslab.inkuire.engine.common.service.SubstitutionMatchService
import org.virtuslab.inkuire.engine.common.service.TopLevelMatchQualityService

class InkuireRunner(
  configReader:        ConfigReader,
  inputHandler:        InputHandler,
  outputHandler:       OutputHandler,
  parser:              BaseSignatureParserService,
  resolver:            InkuireDb => BaseSignatureResolver,
  matchService:        InkuireDb => BaseMatchService,
  matchQualityService: InkuireDb => BaseMatchQualityService,
  prettifier:          BaseSignaturePrettifier
) {

  def run(args: Seq[String]): IO[Unit] =
    configReader
      .readConfig(args)
      .flatMap { config: AppConfig =>
        inputHandler
          .readInput(config)
          .semiflatMap { db: InkuireDb =>
            outputHandler
              .serveOutput()
              .runA(
                Engine.Env(db, matchService(db), matchQualityService(db), prettifier, parser, resolver(db), config)
              )
          }
      }
      .fold(str => println(s"Oooooh man, bad luck. Inkuire encountered an unexpected error. Caused by $str"), identity)

}

object InkuireRunner {
  def scalaRunner(
    configReader:        ConfigReader,
    inputHandler:        InputHandler,
    outputHandler:       OutputHandler,
    parser:              BaseSignatureParserService = new ScalaSignatureParserService,
    resolver:            InkuireDb => BaseSignatureResolver = (db: InkuireDb) => new DefaultSignatureResolver(db),
    matchService:        InkuireDb => BaseMatchService = (db: InkuireDb) => new SubstitutionMatchService(db),
    matchQualityService: InkuireDb => BaseMatchQualityService = (db: InkuireDb) => new TopLevelMatchQualityService(db),
    prettifier:          BaseSignaturePrettifier = new ScalaExternalSignaturePrettifier
  ): InkuireRunner =
    new InkuireRunner(
      configReader,
      inputHandler,
      outputHandler,
      parser,
      resolver,
      matchService,
      matchQualityService,
      prettifier
    )
}
