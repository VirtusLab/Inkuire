package org.virtuslab.inkuire.engine.api

import cats.data.EitherT
import cats.effect.IO
import org.virtuslab.inkuire.engine.model.{AppConfig, InkuireDb}

trait InputHandler {
  def readInput(appConfig: AppConfig): EitherT[IO, String, InkuireDb]
}
