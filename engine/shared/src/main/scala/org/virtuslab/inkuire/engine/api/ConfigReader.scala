package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.api.FutureExcept
import org.virtuslab.inkuire.engine.model.AppConfig

import scala.concurrent.ExecutionContext

trait ConfigReader {
  def readConfig(args: Seq[String])(implicit ec: ExecutionContext): FutureExcept[AppConfig]
}
