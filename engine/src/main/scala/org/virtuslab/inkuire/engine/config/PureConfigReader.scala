package org.virtuslab.inkuire.engine.config

import cats.data.EitherT
import cats.effect.IO
import org.virtuslab.inkuire.engine.api.ConfigReader
import org.virtuslab.inkuire.engine.model.AppConfig
import pureconfig.ConfigSource
import pureconfig._
import pureconfig.generic.auto._
import cats.implicits._

class PureConfigReader extends ConfigReader {
  override def readConfig(args: Seq[String]): EitherT[IO, String, AppConfig] = {
    EitherT {
      IO { ConfigSource.default.load[AppConfig].leftMap(_.prettyPrint()) }
    }
  }
}
