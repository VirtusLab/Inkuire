package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.impl.model.ParsedSignature
import org.virtuslab.inkuire.engine.impl.model.ResolveResult

trait BaseSignatureResolver {
  def resolve(parsed: ParsedSignature): Either[String, ResolveResult]
}
