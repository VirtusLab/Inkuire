package org.virtuslab.inkuire.engine.common.model

import org.virtuslab.inkuire.engine.common.api._

case class Env(
  db:                  InkuireDb,
  matcher:             BaseMatchService,
  matchQualityService: BaseMatchQualityService,
  prettifier:          BaseSignaturePrettifier,
  parser:              BaseSignatureParserService,
  resolver:            BaseSignatureResolver,
  appConfig:           AppConfig
)
