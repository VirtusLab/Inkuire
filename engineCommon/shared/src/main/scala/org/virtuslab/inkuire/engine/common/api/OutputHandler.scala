package org.virtuslab.inkuire.engine.common.api

import cats.data.StateT
import cats.effect.IO
import cats.implicits._
import org.virtuslab.inkuire.engine.common.model.Engine.Env
import org.virtuslab.inkuire.engine.common.model.Engine._

trait OutputHandler {
  def serveOutput(env: Env): IO[Unit] =
    serveOutput().runA(env)

  def serveOutput(): Engine[Unit] = {
    StateT.get[IO, Env] >>= { env =>
      this.serveOutput(env).liftApp
    }
  }
}
