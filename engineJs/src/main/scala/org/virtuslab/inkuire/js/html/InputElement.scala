package org.virtuslab.inkuire.js.html

import monix.eval.Task
import monix.execution.{Ack, Cancelable}
import monix.reactive.{Observable, OverflowStrategy}
import org.scalajs.dom._

import scala.concurrent.duration.DurationInt

class InputElement(input: html.Input) extends BaseInput with BaseOutput {
  def inputChanges: Observable[String] =
    Observable
      .create[String](OverflowStrategy.DropOld(10)) { subscriber =>
        val func = (event: Event) => subscriber.onNext(event.target.asInstanceOf[html.Input].value)
        input.addEventListener("input", func)
        Cancelable(
          () => input.removeEventListener("input", func)
        )
      }
      .debounce(1.seconds)

  def handleResults(results: String): Task[Unit] = Task.now { println(results) }

  def handleNewQuery: Task[Unit] = Task.now { console.clear() }
}

object InputElement {
  def apply(): InputElement = {
    // TODO: ID should be obtained from some config
    val parent = document.getElementById("main")
    val input  = document.createElement("input")
    parent.appendChild(input)
    new InputElement(input.asInstanceOf[html.Input])
  }
}
