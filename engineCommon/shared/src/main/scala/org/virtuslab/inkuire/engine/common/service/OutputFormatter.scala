package org.virtuslab.inkuire.engine.http.http

import org.virtuslab.inkuire.engine.common.model.ExternalSignature
import org.virtuslab.inkuire.engine.common.service.SignaturePrettifier
import org.virtuslab.inkuire.engine.common.model.{Match, ResultFormat}

import collection.JavaConverters._

class OutputFormatter(prettifier: SignaturePrettifier) {
  def createOutput(query: String, signatures: Seq[ExternalSignature]): ResultFormat =
    ResultFormat(
      query,
      fromSignatures(signatures).toList
    )

  private def fromSignatures(signatures: Seq[ExternalSignature]): Seq[Match] =
    signatures
      .zip(prettifier.prettify(signatures).split("\n"))
      .map {
        case (sgn, pretty) =>
          Match(
            pretty,
            sgn.name,
            sgn.packageName,
            sgn.uri
          )
      }
}
