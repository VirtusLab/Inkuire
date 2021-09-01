package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model.{ResolveResult, ParsedSignature}

trait BaseSignatureResolver {
  def resolve(parsed: ParsedSignature): Either[String, ResolveResult]
}
