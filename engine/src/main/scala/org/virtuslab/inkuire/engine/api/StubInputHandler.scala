package org.virtuslab.inkuire.engine.api

import cats.data.EitherT
import cats.effect.IO
import org.virtuslab.inkuire.engine.model.{AppConfig, InkuireDb}

class StubInputHandler(inkuireDb: InkuireDb) extends InputHandler {
  override def readInput(config: AppConfig): EitherT[IO, String, InkuireDb] =
    new EitherT(IO(Right(inkuireDb)): IO[Either[String, InkuireDb]])
}
