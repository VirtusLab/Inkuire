package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.model.Engine.Engine

trait OutputHandler {
  def serveOutput: Engine[Unit]
}
