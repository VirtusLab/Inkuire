package org.virtuslab.inkuire.engine

import org.virtuslab.inkuire.engine.cli.Cli
import org.virtuslab.inkuire.engine.http.HttpServer
import org.virtuslab.inkuire.engine.model.Engine.Env
import org.virtuslab.inkuire.engine.model._
import org.virtuslab.inkuire.engine.parser.KotlinSignatureParserService
import org.virtuslab.inkuire.engine.service.{FluffMatchService, KotlinExternalSignaturePrettifier}

object Main extends App {

  val configReader = new Cli
  val in           = new Cli
  val out          = new HttpServer
  val matchService = (db: InkuireDb) => new FluffMatchService(db)
  val prettifier   = new KotlinExternalSignaturePrettifier
  val parser       = new KotlinSignatureParserService

  configReader
    .readConfig(args)
    .toOption
    .flatMap { config: AppConfig =>
      in.readInput(config)
        .toOption
        .semiflatMap { db: InkuireDb =>
          out
            .serveOutput()
            .runA(
              Env(db, matchService(db), prettifier, parser, config)
            )
        }
    }
    .fold(println("Oooooh man, bad luck. Inkuire encountered an unexpected error :/"))(identity)
    .unsafeRunSync()

}
