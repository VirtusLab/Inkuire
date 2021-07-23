package org.virtuslab.inkuire.js

import org.scalajs.dom._
import org.scalajs.dom.raw.HTMLScriptElement
import org.virtuslab.inkuire.engine.common.model._
import org.virtuslab.inkuire.engine.common.parser._
import org.virtuslab.inkuire.engine.common.service._
import org.virtuslab.inkuire.js.handlers._
import org.scalajs.dom.ext._
import org.scalajs.dom.webworkers.Worker
import org.virtuslab.inkuire.engine.common.model.Engine.Env

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

object Main extends App {

  //This code runs when importing script and we need to filter out cases when its imported by worker
  try {
    window.addEventListener("load", (e: Event) => startApp())
  } catch {
    case e: Exception =>
  }

  def startApp(): Unit = {
    val inkuirePath = Globals.pathToRoot + "scripts/"
    val worker      = new Worker(inkuirePath + "inkuire-worker.js")
  }
}
