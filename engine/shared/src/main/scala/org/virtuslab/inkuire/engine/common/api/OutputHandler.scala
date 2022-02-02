package org.virtuslab.inkuire.engine.common.api

import org.virtuslab.inkuire.engine.common.model.Engine.Env

trait OutputHandler {

  def serveOutput(env: Env): Unit

}
