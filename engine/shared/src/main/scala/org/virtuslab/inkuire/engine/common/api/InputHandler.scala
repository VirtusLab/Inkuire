package org.virtuslab.inkuire.engine.common.api

import org.virtuslab.inkuire.engine.common.model.AppConfig
import org.virtuslab.inkuire.engine.common.model.InkuireDb
import scala.concurrent.ExecutionContext
import org.virtuslab.inkuire.engine.common.api.FutureExcept

trait InputHandler {
  def readInput(appConfig: AppConfig)(implicit ec: ExecutionContext): FutureExcept[InkuireDb]
}
