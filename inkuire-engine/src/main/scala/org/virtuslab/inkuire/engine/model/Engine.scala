package org.virtuslab.inkuire.engine.model

import cats.data.StateT
import cats.effect.IO
import org.virtuslab.inkuire.engine.parser.BaseSignatureParserService
import org.virtuslab.inkuire.engine.service.{BaseMatchService, SignaturePrettifier}

object Engine {
  case class Env(
    db:         InkuireDb,
    matcher:    BaseMatchService,
    prettifier: SignaturePrettifier,
    parser:     BaseSignatureParserService
  )

  type Engine[A] = StateT[IO, Env, A]

  implicit class IOInkuireEngineSyntax[A](f: IO[A]) {
    def liftApp: Engine[A] = StateT.liftF[IO, Env, A](f)
  }
}
