package org.virtuslab.inkuire.engine.parser

import org.virtuslab.inkuire.engine.model.Signature

trait BaseSignatureParserService {
  def parse(str: String): Either[String, Signature]
}
