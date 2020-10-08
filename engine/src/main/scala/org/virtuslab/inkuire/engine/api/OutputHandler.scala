package org.virtuslab.inkuire.engine.api

import cats.implicits._
import cats.data.StateT
import cats.effect.IO
import org.virtuslab.inkuire.engine.model.Engine._

trait OutputHandler {
  def serveOutput(env: Env): IO[Unit] =
    serveOutput().runA(env)

  def serveOutput(): Engine[Unit] = {
    StateT.get[IO, Env] >>= { env =>
      this.serveOutput(env).liftApp
    }
  }
}
