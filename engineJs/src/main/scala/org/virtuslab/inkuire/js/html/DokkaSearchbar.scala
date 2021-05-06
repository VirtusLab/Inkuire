package org.virtuslab.inkuire.js.html

import io.circe.syntax._
import io.circe.parser._
import io.circe.generic.auto._
import monix.eval.Task
import monix.execution.Cancelable
import monix.reactive.{Observable, OverflowStrategy}
import org.scalajs.dom._
import org.scalajs.dom.webworkers.Worker
import org.virtuslab.inkuire.js.Globals
import org.virtuslab.inkuire.model.{Match, OutputFormat}
import monix.execution.Scheduler.Implicits.global
import org.scalajs.dom.html.Input

import scala.concurrent.duration.DurationInt

//TODO: Separate it to another project
class DokkaSearchbar(val worker: Worker) {
  private def handleResults(results: OutputFormat): Task[Unit] =
    Task.now {
      val res = results.matches.map(matchToResult)
      res.foreach(resultsDiv.appendChild)
    }

  private def handleNewQuery: Task[Unit] =
    Task.now {
      while (resultsDiv.hasChildNodes()) resultsDiv.removeChild(resultsDiv.lastChild)
    }

  def lockInput(input: Input): Unit = {
    input.placeholder = "Wait until Inkuire engines initializes"
    input.disabled = true
  }

  def unlockInput(input: Input): Unit = {
    input.placeholder = "Search for function by signature..."
    input.disabled = false
  }

  private val registerOnMessage: Unit = {
    val func = (msg: MessageEvent) => {
      msg.data.asInstanceOf[String] match {
        case "engine_ready" => registerInputChanges
        case "new_query"    => handleNewQuery
        case other          => parse(other).map(_.as[OutputFormat]).map(_.map(handleResults))
      }
    }
    worker.addEventListener("message", func)
  }

  private val logoClick: html.Span = {
    val element = document.createElement("span").asInstanceOf[html.Span]
    element.id = "inkuire-search"
    element.onclick = (event: Event) =>
      if (rootDiv.className.contains("hidden"))
        rootDiv.className = rootShowClasses
      else rootDiv.className = rootHiddenClasses
    document.getElementById("searchBar").appendChild(element)
    element
  }

  private val input: html.Input = {
    val element = document.createElement("input").asInstanceOf[html.Input]
    element.id = "inkuire-input"
    lockInput(element)
    element
  }

  private val resultsDiv: html.Div = {
    val element = document.createElement("div").asInstanceOf[html.Div]
    element.id = "inkuire-results"
    element
  }

  private val rootHiddenClasses = "hidden"
  private val rootShowClasses   = ""
  private val rootDiv: html.Div = {
    val element = document.createElement("div").asInstanceOf[html.Div]
    element.addEventListener("click", (e: Event) => e.stopPropagation())
    logoClick.addEventListener("click", (e: Event) => e.stopPropagation())
    document.body.addEventListener("click", (e: Event) => element.className = rootHiddenClasses)
    element.className = rootHiddenClasses
    element.id = "inkuire-searchbar"
    element.appendChild(input)
    element.appendChild(resultsDiv)
    document.body.appendChild(element)
    element
  }

  private def matchToResult(mtch: Match) = {
    val wrapper = document.createElement("div")
    wrapper.classList.add("inkuire-result")

    val resultA = document.createElement("a").asInstanceOf[html.Anchor]
    resultA.href = Globals.pathToRoot + mtch.pageLocation
    resultA.text = s"${mtch.functionName}: ${mtch.prettifiedSignature}"

    val location = document.createElement("span")
    location.classList.add("pull-right")
    location.classList.add("inkuire-location")
    location.textContent = mtch.packageLocation

    wrapper.appendChild(resultA)
    wrapper.appendChild(location)
    wrapper
  }

  def registerInputChanges: Unit = {
    Observable
      .create[String](OverflowStrategy.DropOld(10)) { subscriber =>
        val func = (event: Event) => subscriber.onNext(event.target.asInstanceOf[html.Input].value)
        input.addEventListener("input", func)
        Cancelable(() => input.removeEventListener("input", func))
      }
      .debounce(1.seconds)
      .doOnSubscribe { Task.now { unlockInput(input) } }
      .foreach((query) => worker.postMessage(query))
  }

}
