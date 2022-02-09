package org.virtuslab.inkuire.engine.api

import scala.concurrent.ExecutionContext

trait InputHandler {
  def readInput(args: Seq[String])(implicit ec: ExecutionContext): FutureExcept[InkuireDb]
}
