package org.virtuslab.inkuire.engine.common.model

import cats.data.StateT
import cats.effect.IO
import org.virtuslab.inkuire.engine.common.api._

object Engine {
  case class Env(
    db:                  InkuireDb,
    matcher:             BaseMatchService,
    matchQualityService: BaseMatchQualityService,
    prettifier:          BaseSignaturePrettifier,
    parser:              BaseSignatureParserService,
    resolver:            BaseSignatureResolver,
    appConfig:           AppConfig
  )

  type Engine[A] = StateT[IO, Env, A]

  implicit class IOInkuireEngineSyntax[A](f: IO[A]) {
    def liftApp: Engine[A] = StateT.liftF[IO, Env, A](f)
  }
}
