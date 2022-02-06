package org.virtuslab.inkuire.engine.common.api

import org.virtuslab.inkuire.engine.common.model.AppConfig
import scala.concurrent.ExecutionContext
import org.virtuslab.inkuire.engine.common.api.FutureExcept

trait ConfigReader {
  def readConfig(args: Seq[String])(implicit ec: ExecutionContext): FutureExcept[AppConfig]
}
