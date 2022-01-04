package org.virtuslab.inkuire.engine.common.api

import cats.data.EitherT
import cats.effect.IO
import org.virtuslab.inkuire.engine.common.model.AppConfig
import org.virtuslab.inkuire.engine.common.model.InkuireDb

trait InputHandler {
  def readInput(appConfig: AppConfig): EitherT[IO, String, InkuireDb]
}
