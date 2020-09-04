package org.virtuslab.inkuire.engine.http

import org.virtuslab.inkuire.model.OutputFormat
import scalatags.Text.all._
import collection.JavaConverters._

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

  def result(results: OutputFormat): String = {
    html(
      body(
        h1(s"Result for: ${results.getQuery}"),
        table(style := "width: 100%;")(
          tr(
            th(style := "text-align: left")("Name"),
            th(style := "text-align: left")("Signature"),
            th(style := "text-align: left")("Localization")
          ),
          for (res <- results.getMatches.asScala.toList)
            yield
              tr(
                td(res.getFunctionName),
                td(res.getPrettifiedSignature),
                td(res.getLocalization)
              )
        )
      )
    ).toString()
  }
}
