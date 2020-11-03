package org.virtuslab.inkuire.js
import org.scalajs.dom._
import org.scalajs.dom.raw.HTMLScriptElement
import org.virtuslab.inkuire.engine.common.model._
import org.virtuslab.inkuire.engine.common.parser._
import org.virtuslab.inkuire.engine.common.service._
import org.virtuslab.inkuire.js.handlers._
import org.virtuslab.inkuire.js.html.DokkaSearchbar
import org.scalajs.dom.ext._
import org.virtuslab.inkuire.engine.common.model.Engine.Env

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

object Main extends App {

  window.addEventListener("load", (e: Event) => startApp())

  def startApp(): Unit = {
    val scriptPath   = Globals.pathToRoot + "scripts/"
    val searchbar    = new DokkaSearchbar()
    val configReader = new JSInputHandler(scriptPath)
    val in           = new JSInputHandler(scriptPath)
    val out          = new JSOutputHandler(searchbar, searchbar)
    val matchService = (db: InkuireDb) => new FluffMatchService(db)
    val prettifier   = new KotlinExternalSignaturePrettifier
    val resolver     = (db: InkuireDb) => new DefaultSignatureResolver(db.types)
    val parser       = new KotlinSignatureParserService

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
