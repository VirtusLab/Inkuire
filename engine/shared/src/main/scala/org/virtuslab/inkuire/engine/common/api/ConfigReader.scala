package org.virtuslab.inkuire.engine.common.api

import org.virtuslab.inkuire.engine.common.model.AppConfig
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import org.virtuslab.inkuire.engine.common.utils.fp._

trait ConfigReader {
  def readConfig(args: Seq[String])(implicit ec: ExecutionContext): EitherT[Future, String, AppConfig]
}
