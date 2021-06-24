package org.virtuslab.inkuire.js.worker

import io.circe.generic.auto._
import io.circe.syntax._
import monix.eval.Task
import monix.execution.Cancelable
import monix.reactive._
import org.scalajs.dom.{html, Event}
import org.scalajs.dom.raw.MessageEvent
import org.scalajs.dom.webworkers.{DedicatedWorkerGlobalScope, Worker}
import org.virtuslab.inkuire.js.worker.JSHandler
import org.virtuslab.inkuire.engine.common.model.ResultFormat

class InkuireWorker(self: DedicatedWorkerGlobalScope) extends JSHandler {
  override def inputChanges: Observable[String] =
    Observable
      .create[String](OverflowStrategy.DropOld(10)) { subscriber =>
        val func = (event: Event) => subscriber.onNext(event.asInstanceOf[MessageEvent].data.asInstanceOf[String])
        self.addEventListener("message", func)
        Cancelable(() => self.removeEventListener("message", func))
      }

  override def handleResults(results: ResultFormat): Task[Unit] =
    Task.now { self.postMessage(results.asJson.toString()) }

  override def handleNewQuery: Task[Unit] =
    Task.now { self.postMessage("new_query") }

  override def handleQueryEnded(msg: String): Task[Unit] =
    Task.now { self.postMessage(s"query_ended$msg") }

  override def notifyEngineReady: Task[Unit] = Task.now { self.postMessage("engine_ready") }
}
