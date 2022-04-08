package org.virtuslab.inkuire.engine.impl.service

import org.virtuslab.inkuire.engine.api.BaseSignaturePrettifier
import org.virtuslab.inkuire.engine.impl.model.AnnotatedSignature
import org.virtuslab.inkuire.engine.impl.model.Match
import org.virtuslab.inkuire.engine.impl.model.ResultFormat

class OutputFormatter(prettifier: BaseSignaturePrettifier) {
  def createOutput(query: String, signatures: Seq[(AnnotatedSignature, Int)]): ResultFormat =
    ResultFormat(
      query,
      fromSignatures(signatures).toList
    )

  private def fromSignatures(signatures: Seq[(AnnotatedSignature, Int)]): Seq[Match] =
    signatures
      .zip(signatures.map { case (sgn, _) => prettifier.prettify(sgn) })
      .map {
        case ((sgn, mq), pretty) =>
          Match(
            pretty,
            sgn.name,
            sgn.packageName,
            sgn.uri,
            sgn.entryType,
            mq
          )
      }
}
