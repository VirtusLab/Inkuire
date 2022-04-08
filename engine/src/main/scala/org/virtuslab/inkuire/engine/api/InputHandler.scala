package org.virtuslab.inkuire.engine.api

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait InputHandler {
  def readInput(args: Seq[String])(implicit ec: ExecutionContext): Future[Either[String, InkuireDb]]
}
