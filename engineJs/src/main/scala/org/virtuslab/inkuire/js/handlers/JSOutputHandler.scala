package org.virtuslab.inkuire.js.handlers

import cats.effect.IO
import org.scalajs.dom.console
import org.virtuslab.inkuire.engine.common.api.OutputHandler
import org.virtuslab.inkuire.engine.common.model.Engine
import org.virtuslab.inkuire.js.html.InputElement

class JSOutputHandler extends OutputHandler {
  override def serveOutput(env: Engine.Env): IO[Unit] = {
    def executeQuery(query: String): Either[String, String] = {
      env.parser
        .parse(query)
        .map(env.matcher.|??|)
        .map(env.prettifier.prettify)
    }

    IO.async(_ => {
      val input = InputElement()
      input.registerOnInputChange { (value: String) =>
        executeQuery(value) match {
          case Right(v) => console.log(v)
          case Left(v)  => console.log(s"ERROR: $v")
        }
      }
    })
  }
}
