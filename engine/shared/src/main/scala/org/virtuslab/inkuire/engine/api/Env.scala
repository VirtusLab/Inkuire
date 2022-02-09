package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.impl.model._
import org.virtuslab.inkuire.engine.impl.service._

case class Env(
  db:                  InkuireDb,
  matcher:             BaseMatchService,
  matchQualityService: BaseMatchQualityService,
  prettifier:          BaseSignaturePrettifier,
  parser:              BaseSignatureParserService,
  resolver:            BaseSignatureResolver
) {
  def run(signature: String): Either[String, ResultFormat] = {
    val formatter = new OutputFormatter(prettifier)
    parser
      .parse(signature)
      .flatMap(resolver.resolve)
      .map(matcher.findMatches)
      .map(matchQualityService.sortMatches)
      .map(formatter.createOutput(signature, _))
  }
}
