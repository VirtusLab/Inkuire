package org.virtuslab.inkuire.engine.common.model

import org.virtuslab.inkuire.engine.common.api._
import org.virtuslab.inkuire.engine.common.utils.State

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

  type Engine[A] = State[Env, A]
}
