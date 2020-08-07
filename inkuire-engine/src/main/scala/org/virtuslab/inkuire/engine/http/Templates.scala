package org.virtuslab.inkuire.engine.http

import scalatags.Text.all._

object Templates {
  def formTemplate(): String = {
    html(
      body(
        form(method := "post")(
          input(name := "query"),
          input(`type` := "submit", value := "Send")
        )
      )
    ).toString()
  }

  def result(results: List[String]): String = {
    html(
      body(
        for (res <- results) yield p(res)
      )
    ).toString()
  }
}
