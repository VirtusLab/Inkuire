package org.virtuslab.inkuire.js.html

import monix.eval.Task
import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global
import org.virtuslab.inkuire.model.OutputFormat
import scala.scalajs.js.Date
import scala.util.Try

trait BaseOutput {
  //TODO: Consider configuring it
  def resultLimit: Option[Long] = None


  def timeElapsed(start: Double) = () => println(Date.now() - start)

  /* It's a common pattern for cases when you need to switch to new computation before end of previous computation.
  Switchmap emits elements from most recently emitted child observable
   */
  def registerOutput(obs: Observable[(Double, Observable[OutputFormat])]): Unit = {
    obs
      .switchMap { case (start: Double, o: Observable[OutputFormat]) =>
        val results = o.doOnStart(_ => handleNewQuery).doOnCompleteF(timeElapsed(start))
        resultLimit.fold(results)(results.take)
      }
      .foreach(handleResults)
//      .onComplete(_ => Try(timeElapsed(x)))

//      .doOnComplete(timeElapsed(start))
  }

  def handleResults(results: OutputFormat): Task[Unit]

  def handleNewQuery: Task[Unit]

}
