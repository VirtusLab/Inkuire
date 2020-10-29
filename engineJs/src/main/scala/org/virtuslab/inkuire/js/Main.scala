package org.virtuslab.inkuire.js

import org.virtuslab.inkuire.engine.common.model.Engine.Env
import org.virtuslab.inkuire.engine.common.model.{AppConfig, InkuireDb}
import org.virtuslab.inkuire.engine.common.parser.KotlinSignatureParserService
import org.virtuslab.inkuire.engine.common.service.{FluffMatchService, KotlinExternalSignaturePrettifier}
import org.virtuslab.inkuire.js.handlers.{JSInputHandler, JSOutputHandler}

object Main extends App {

  val configReader = new JSInputHandler
  val in           = new JSInputHandler
  val out          = new JSOutputHandler
  val matchService = (db: InkuireDb) => new FluffMatchService(db)
  val prettifier   = new KotlinExternalSignaturePrettifier
  val parser       = new KotlinSignatureParserService

  //TODO: I don't know how to pass link to config dynamically, because JS doesn't have CLI params
  configReader
    .readConfig(Seq("./inkuire-config.json"))
    .flatMap { config: AppConfig =>
      in.readInput(config)
        .semiflatMap { db: InkuireDb =>
          out
            .serveOutput()
            .runA(
              Env(db, matchService(db), prettifier, parser, config)
            )
        }
    }
    .fold(str => println(s"Oooooh man, bad luck. Inkuire encountered an unexpected error. Caused by $str"), identity)
    .unsafeRunAsyncAndForget()

}
