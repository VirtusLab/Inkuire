package org.virtuslab.inkuire.engine.model

import org.virtuslab.inkuire.engine.api._

case class Env(
  db:                  InkuireDb,
  matcher:             BaseMatchService,
  matchQualityService: BaseMatchQualityService,
  prettifier:          BaseSignaturePrettifier,
  parser:              BaseSignatureParserService,
  resolver:            BaseSignatureResolver,
  appConfig:           AppConfig
)
