package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.model.ParsedSignature

trait BaseSignatureParserService {
  def parse(str: String): Either[String, ParsedSignature]

  def parseError(msg: String): String = s"Parsing error: $msg"
}
