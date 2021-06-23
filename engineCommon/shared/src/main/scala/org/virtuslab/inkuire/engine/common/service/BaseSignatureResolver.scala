package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model.{ResolveResult, Signature}

trait BaseSignatureResolver {
  def resolve(parsed: Signature): Either[String, ResolveResult]
}
