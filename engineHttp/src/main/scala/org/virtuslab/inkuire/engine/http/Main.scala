package org.virtuslab.inkuire.engine.http

import org.virtuslab.inkuire.engine.common.model.AppConfig
import org.virtuslab.inkuire.engine.common.model.Engine.Env
import org.virtuslab.inkuire.engine.common.model.InkuireDb
import org.virtuslab.inkuire.engine.common.parser.ScalaSignatureParserService
import org.virtuslab.inkuire.engine.common.service._
import org.virtuslab.inkuire.engine.http.cli.Cli
import org.virtuslab.inkuire.engine.http.http.HttpServer

object Main {
  def main(args: Array[String]): Unit = {
    val configReader = new Cli
    val in           = new Cli
    val out          = new HttpServer
    val matchService: InkuireDb => FluffMatchService = (db: InkuireDb) => new FluffMatchService(db)
    val matchQualityService: InkuireDb => TopLevelMatchQualityService = (db: InkuireDb) =>
      new TopLevelMatchQualityService(db)
    val prettifier = new ScalaExternalSignaturePrettifier
    val resolver: InkuireDb => DefaultSignatureResolver = (db: InkuireDb) => new DefaultSignatureResolver(db)
    val parser = new ScalaSignatureParserService

    configReader
      .readConfig(args.toSeq)
      .flatMap { (config: AppConfig) =>
        in.readInput(config)
          .semiflatMap { (db: InkuireDb) =>
            out.serveOutput()
              .runA(
                Env(db, matchService(db), matchQualityService(db), prettifier, parser, resolver(db), config)
              )
          }
      }
      .fold(str => println(s"Oooooh man, bad luck. Inkuire encountered an unexpected error. Caused by $str"), identity)
      .unsafeRunSync()
  }
}
