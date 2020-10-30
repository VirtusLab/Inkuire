package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model.{ResolveResult, Signature}

trait BaseSignatureResolver {
  //Can be changed to Either[String, ResolveResult] to report resolve errors (unknown types etc.)
  def resolve(parsed: Signature): ResolveResult
}
