package org.virtuslab.inkuire.engine.http.config

import cats.data.EitherT
import cats.effect.IO
import pureconfig.ConfigSource
import pureconfig._
import pureconfig.generic.auto._
import cats.implicits._
import org.virtuslab.inkuire.engine.common.api
import org.virtuslab.inkuire.engine.common.api.ConfigReader
import org.virtuslab.inkuire.engine.common.model.AppConfig

class PureConfigReader extends api.ConfigReader {
  override def readConfig(args: Seq[String]): EitherT[IO, String, AppConfig] = {
    EitherT {
      IO { ConfigSource.default.load[AppConfig].leftMap(_.prettyPrint()) }
    }
  }
}
