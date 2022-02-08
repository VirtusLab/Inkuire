package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.api.FutureExcept
import org.virtuslab.inkuire.engine.model.AppConfig
import org.virtuslab.inkuire.engine.model.InkuireDb

import scala.concurrent.ExecutionContext

trait InputHandler {
  def readInput(appConfig: AppConfig)(implicit ec: ExecutionContext): FutureExcept[InkuireDb]
}
