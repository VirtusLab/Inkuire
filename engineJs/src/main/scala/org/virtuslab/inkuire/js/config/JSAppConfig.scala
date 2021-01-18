package org.virtuslab.inkuire.js.config

import org.virtuslab.inkuire.engine.common.model._
import org.virtuslab.inkuire.engine.common.utils.syntax.AnyInkuireSyntax

case class JSAppConfig(
  dbPaths:            Seq[DbPath],
  ancestryGraphPaths: Seq[AncestryGraphPath]
) extends AppConfig

object JSAppConfig {
  def validate(appConfig: AppConfig): Either[String, JSAppConfig] = appConfig match {
    case j: JSAppConfig => j.right
    case _ => "Provided app config is not an instance of JSAppConfig".left
  }
}
