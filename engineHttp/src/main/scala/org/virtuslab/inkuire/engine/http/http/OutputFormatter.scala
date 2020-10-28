package org.virtuslab.inkuire.engine.http.http

import org.virtuslab.inkuire.engine.common.model.ExternalSignature
import org.virtuslab.inkuire.engine.common.service.SignaturePrettifier
import org.virtuslab.inkuire.model.{Match, OutputFormat}

import collection.JavaConverters._

class OutputFormatter(prettifier: SignaturePrettifier) {
  def createOutput(query: String, signatures: Seq[ExternalSignature]): OutputFormat = new OutputFormat(
    query,
    fromSignatures(signatures).toList
  )

  private def fromSignatures(signatures: Seq[ExternalSignature]): Seq[Match] =
    signatures
      .zip(prettifier.prettify(signatures).split("\n"))
      .map {
        case (sgn, pretty) =>
          new Match(
            pretty,
            sgn.name,
            sgn.uri
          )
      }
}
