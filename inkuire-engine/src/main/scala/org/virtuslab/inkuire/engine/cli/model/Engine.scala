package org.virtuslab.inkuire.engine.cli.model

import cats.data.StateT
import cats.effect.IO
import org.virtuslab.inkuire.engine.model.InkuireDb

object Engine {
  case class Env(
    db: InkuireDb,
    dbPath: String
  )

  type Engine[A] = StateT[IO, Env, A]

  implicit class IOInkuireEngineSyntax[A](f: IO[A]) {
    def liftApp: Engine[A] = StateT.liftF[IO, Env, A](f)
  }
}
