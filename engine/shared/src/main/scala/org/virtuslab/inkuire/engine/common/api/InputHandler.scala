package org.virtuslab.inkuire.engine.common.api

import org.virtuslab.inkuire.engine.common.api.FutureExcept
import org.virtuslab.inkuire.engine.common.model.AppConfig
import org.virtuslab.inkuire.engine.common.model.InkuireDb

import scala.concurrent.ExecutionContext

trait InputHandler {
  def readInput(appConfig: AppConfig)(implicit ec: ExecutionContext): FutureExcept[InkuireDb]
}
