package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.model.ParsedSignature
import org.virtuslab.inkuire.engine.model.ResolveResult

trait BaseSignatureResolver {
  def resolve(parsed: ParsedSignature): Either[String, ResolveResult]
}
