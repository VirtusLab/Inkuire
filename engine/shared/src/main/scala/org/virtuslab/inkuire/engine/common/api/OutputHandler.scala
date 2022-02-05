package org.virtuslab.inkuire.engine.common.api

import org.virtuslab.inkuire.engine.common.model.Engine.Env
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait OutputHandler {

  def serveOutput(env: Env)(implicit ec: ExecutionContext): Future[Unit]

}
