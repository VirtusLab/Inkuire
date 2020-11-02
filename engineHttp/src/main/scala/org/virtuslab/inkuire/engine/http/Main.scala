package org.virtuslab.inkuire.engine.http

import org.virtuslab.inkuire.engine.http.cli.Cli
import org.virtuslab.inkuire.engine.common.model.Engine.Env
import org.virtuslab.inkuire.engine.common.model.{AppConfig, InkuireDb}
import org.virtuslab.inkuire.engine.common.parser.KotlinSignatureParserService
import org.virtuslab.inkuire.engine.common.service._
import org.virtuslab.inkuire.engine.http.http.HttpServer

object Main extends App {

  val configReader = new Cli
  val in           = new Cli
  val out          = new HttpServer
  val matchService = (db: InkuireDb) => new FluffMatchService(db)
  val prettifier   = new KotlinExternalSignaturePrettifier
  val resolver     = (db: InkuireDb) => new DefaultSignatureResolver(db.types)
  val parser       = new KotlinSignatureParserService

  configReader
    .readConfig(args)
    .flatMap { config: AppConfig =>
      in.readInput(config)
        .semiflatMap { db: InkuireDb =>
          out
            .serveOutput()
            .runA(
              Env(db, matchService(db), prettifier, parser, resolver(db), config)
            )
        }
    }
    .fold(str => println(s"Oooooh man, bad luck. Inkuire encountered an unexpected error. Caused by $str"), identity)
    .unsafeRunSync()

}
