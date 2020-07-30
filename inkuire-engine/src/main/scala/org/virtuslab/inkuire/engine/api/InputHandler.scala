package org.virtuslab.inkuire.engine.api

import cats.data.EitherT
import cats.effect.IO
import org.virtuslab.inkuire.engine.model.InkuireDb

trait InputHandler {
  def readInput(args: Seq[String]): EitherT[IO, String, InkuireDb]
}
