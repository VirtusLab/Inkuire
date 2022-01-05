package org.virtuslab.inkuire.engine.common.api

import org.virtuslab.inkuire.engine.common.model.ParsedSignature

trait BaseSignatureParserService {
  def parse(str: String): Either[String, ParsedSignature]

  def parseError(msg: String): String = s"Parsing error: $msg"
}
