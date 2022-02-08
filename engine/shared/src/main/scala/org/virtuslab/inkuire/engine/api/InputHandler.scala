package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.api.FutureExcept
import org.virtuslab.inkuire.engine.impl.model.InkuireDb

import scala.concurrent.ExecutionContext

trait InputHandler {
  def readInput(args: Seq[String])(implicit ec: ExecutionContext): FutureExcept[InkuireDb]
}
