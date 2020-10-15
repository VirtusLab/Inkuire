package org.virtuslab.inkuire.engine.api

import cats.data.EitherT
import cats.effect.IO
import org.virtuslab.inkuire.engine.model.AppConfig

trait ConfigReader {
  def readConfig(args: Seq[String]): EitherT[IO, String, AppConfig]
}
