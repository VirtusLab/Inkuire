package org.virtuslab.inkuire.engine.http

import scalatags.Text.all._

object Templates {
  def formTemplate(): String = {
    html(
      body(style := "display: flex; justify-content: center; align-items: center;")(
        div(style := "vertical-align: middle;text-align: center; width: 600px")(
          form(method := "post")(
            div(
              input(name := "query", style := "width: 100%")
            ),
            div(
              input(`type` := "submit", value := "Send")
            )
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
