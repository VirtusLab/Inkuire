package org.virtuslab.inkuire.engine.http

import scalatags.Text.all._

object Templates {
  def formTemplate(): String = {
    html(
      body(
        form(method := "post")(
          div(style := "text-align: center")(
            input(name := "query")
          ),
          div(style := "text-align: center")(
            input(`type` := "submit", value := "Send")
          )
        )
      )
    ).toString()
  }

  def result(results: List[String]): String = {
    html(
      body(
        div(style := "text-align: center")(
          for (res <- results) yield p(res)
        )
      )
    ).toString()
  }
}
