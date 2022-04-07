package org.virtuslab.inkuire.engine.impl.model

case class ResolveResult(
  signatures: Seq[Signature],
  filters:    SignatureFilters
)
