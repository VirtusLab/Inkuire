package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.impl.model.ParsedSignature

trait BaseSignatureParserService {
  def parse(str: String): Either[String, ParsedSignature]
}
