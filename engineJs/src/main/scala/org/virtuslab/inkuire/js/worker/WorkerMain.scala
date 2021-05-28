package org.virtuslab.inkuire.js.worker

import org.scalajs.dom.raw.DedicatedWorkerGlobalScope
import org.scalajs.dom.webworkers.WorkerGlobalScope
import org.virtuslab.inkuire.engine.common.model.Engine.Env
import org.virtuslab.inkuire.engine.common.model.{AppConfig, InkuireDb}
import org.virtuslab.inkuire.engine.common.parser.ScalaSignatureParserService
import org.virtuslab.inkuire.engine.common.service._
import org.virtuslab.inkuire.js.Globals
import org.virtuslab.inkuire.js.handlers.{JSInputHandler, JSOutputHandler}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

// This code is used to generate function that will be called as initializer by worker
@JSExportTopLevel("WorkerMain")
object WorkerMain {
  def self: DedicatedWorkerGlobalScope = DedicatedWorkerGlobalScope.self

  @JSExport("main")
  def main(): Unit = {
    val scriptPath   = ""
    val handler      = new InkuireWorker(self)
    val configReader = new JSInputHandler(scriptPath)
    val in           = new JSInputHandler(scriptPath)
    val out          = new JSOutputHandler(handler)
    val matchService = (db: InkuireDb) => new FluffMatchService(db)
    val prettifier   = new ScalaExternalSignaturePrettifier
    val resolver     = (db: InkuireDb) => new DefaultSignatureResolver(db.types)
    val parser       = new ScalaSignatureParserService

    configReader
      .readConfig(Seq(scriptPath + "inkuire-config.json"))
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
      .fold(
        str => println(s"Oooooh man, bad luck. Inkuire encountered an unexpected error. Caused by $str"),
        identity
      )
      .unsafeRunAsyncAndForget()
  }

}
