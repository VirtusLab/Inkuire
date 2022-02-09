package org.virtuslab.inkuire.js.worker

import org.scalajs.dom.raw.DedicatedWorkerGlobalScope
import org.virtuslab.inkuire.engine.api.InkuireRunner
import org.virtuslab.inkuire.js.handlers.JSInputHandler
import org.virtuslab.inkuire.js.handlers.JSOutputHandler

import scala.annotation.nowarn
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

// This code is used to generate function that will be called as initializer by worker
@nowarn("cat=other") // TODO(kπ) investigate
@JSExportTopLevel("WorkerMain")
object WorkerMain {
  def self: DedicatedWorkerGlobalScope = DedicatedWorkerGlobalScope.self

  @JSExport("main")
  def main(): Unit = {
    val scriptPath = ""

    InkuireRunner
      .scalaRunner(
        new JSInputHandler(scriptPath),
        new JSOutputHandler(new InkuireWorker(self))
      )
      .run(Seq(scriptPath + "inkuire-config.json"))
  }

}
