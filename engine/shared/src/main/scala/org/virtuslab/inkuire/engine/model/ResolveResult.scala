package org.virtuslab.inkuire.engine.model

case class ResolveResult(
  signatures: Seq[Signature],
  filters:    SignatureFilters
)
