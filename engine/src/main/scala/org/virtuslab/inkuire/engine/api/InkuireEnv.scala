package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.impl.model._
import org.virtuslab.inkuire.engine.impl.service._

case class InkuireEnv(
  db:                  InkuireDb,
  parser:              BaseSignatureParserService,
  resolver:            BaseSignatureResolver,
  matcher:             BaseMatchService,
  matchQualityService: BaseMatchQualityService,
  prettifier:          BaseSignaturePrettifier
) {
  def query(signature: String): Either[String, Seq[AnnotatedSignature]] =
    queryRaw(signature).map(_.map(_._1))

  def prettify(signature: AnnotatedSignature): String =
    prettifier.prettify(signature)

  def queryRaw(signature: String): Either[String, Seq[(AnnotatedSignature, Int)]] =
    parser
      .parse(signature)
      .flatMap(resolver.resolve)
      .map(matcher.findMatches)
      .map(matchQualityService.sortMatches)
}

object InkuireEnv {
  def defaultScalaEnv(
    parser:              BaseSignatureParserService = new ScalaSignatureParserService,
    resolver:            InkuireDb => BaseSignatureResolver = (db: InkuireDb) => new DefaultSignatureResolver(db),
    matchService:        InkuireDb => BaseMatchService = (db: InkuireDb) => new SubstitutionMatchService(db),
    matchQualityService: InkuireDb => BaseMatchQualityService = (db: InkuireDb) => new TopLevelMatchQualityService(db),
    prettifier:          BaseSignaturePrettifier = new ScalaAnnotatedSignaturePrettifier
  ): InkuireDb => InkuireEnv = { (db: InkuireDb) =>
    InkuireEnv(
      db,
      parser,
      resolver(db),
      matchService(db),
      matchQualityService(db),
      prettifier
    )
  }
}
