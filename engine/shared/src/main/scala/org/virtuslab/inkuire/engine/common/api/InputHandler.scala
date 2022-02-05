package org.virtuslab.inkuire.engine.common.api

import org.virtuslab.inkuire.engine.common.model.AppConfig
import org.virtuslab.inkuire.engine.common.model.InkuireDb
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import org.virtuslab.inkuire.engine.common.utils.fp._

trait InputHandler {
  def readInput(appConfig: AppConfig)(implicit ec: ExecutionContext): EitherT[Future, String, InkuireDb]
}
