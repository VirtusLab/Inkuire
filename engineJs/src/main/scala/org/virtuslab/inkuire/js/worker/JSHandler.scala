package org.virtuslab.inkuire.js.worker

import monix.eval.Task
import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global
import org.virtuslab.inkuire.engine.common.model.OutputFormat

trait JSHandler {

  //TODO: Consider configuring it
  def resultLimit: Option[Long] = None

  /* It's a common pattern for cases when you need to switch to new computation before end of previous computation.
  Switchmap emits elements from most recently emitted child observable
   */
  def registerOutput(obs: Observable[Observable[OutputFormat]]): Unit = {
    obs
      .switchMap { (o: Observable[OutputFormat]) =>
        val results = o.doOnStart(_ => handleNewQuery)
        resultLimit.fold(results)(results.take)
      }
      .foreach(handleResults)
  }

  def handleResults(results: OutputFormat): Task[Unit]

  def handleNewQuery: Task[Unit]

  def inputChanges: Observable[String]

  def notifyEngineReady: Task[Unit]
}