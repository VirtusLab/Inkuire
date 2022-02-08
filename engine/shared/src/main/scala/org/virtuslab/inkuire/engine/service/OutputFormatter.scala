package org.virtuslab.inkuire.http

import org.virtuslab.inkuire.engine.api.BaseSignaturePrettifier
import org.virtuslab.inkuire.engine.model.ExternalSignature
import org.virtuslab.inkuire.engine.model.Match
import org.virtuslab.inkuire.engine.model.ResultFormat

class OutputFormatter(prettifier: BaseSignaturePrettifier) {
  def createOutput(query: String, signatures: Seq[(ExternalSignature, Int)]): ResultFormat =
    ResultFormat(
      query,
      fromSignatures(signatures).toList
    )

  private def fromSignatures(signatures: Seq[(ExternalSignature, Int)]): Seq[Match] =
    signatures
      .zip(prettifier.prettify(signatures.map(_._1)).split("\n"))
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
