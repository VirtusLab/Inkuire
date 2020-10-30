package org.virtuslab.inkuire.js.html

import monix.eval.Task
import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global

trait BaseOutput {
  //TODO: Consider configuring it
  def resultLimit: Option[Long] = None

  /* It's a common pattern for cases when you need to switch to new computation before end of previous computation.
  Switchmap emits elements from most recently emitted child observable
   */
  def registerOutput(obs: Observable[Observable[String]]): Unit = {
    obs
      .switchMap { (o: Observable[String]) =>
        val results = o.doOnStart(_ => handleNewQuery)
        resultLimit.fold(results)(results.take)
      }
      .foreach(handleResults)
  }

  def handleResults(results: String): Task[Unit]

  def handleNewQuery: Task[Unit]
}
