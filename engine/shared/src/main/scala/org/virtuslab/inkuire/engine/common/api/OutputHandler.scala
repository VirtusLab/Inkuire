package org.virtuslab.inkuire.engine.common.api

import org.virtuslab.inkuire.engine.common.model.Env

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait OutputHandler {

  def serveOutput(env: Env)(implicit ec: ExecutionContext): Future[Unit]

}
