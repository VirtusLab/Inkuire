package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.api.InkuireEnv

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait OutputHandler {
  def serveOutput(env: InkuireEnv)(implicit ec: ExecutionContext): Future[Unit]
}
