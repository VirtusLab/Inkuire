package org.virtuslab.inkuire.engine.common.api

import cats.data.EitherT
import cats.effect.IO
import org.virtuslab.inkuire.engine.common.model.AppConfig

trait ConfigReader {
  def readConfig(args: Seq[String]): EitherT[IO, String, AppConfig]
}
