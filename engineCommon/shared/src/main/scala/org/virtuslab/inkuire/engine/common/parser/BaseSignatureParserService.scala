package org.virtuslab.inkuire.engine.common.parser

import org.virtuslab.inkuire.engine.common.model.Signature

trait BaseSignatureParserService {
  def parse(str: String): Either[String, Signature]
}
