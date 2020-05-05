package org.virtuslab.inkuire.engine.model

case class Signature(
  receiver: Option[Type],
  arguments: Seq[Type],
  result: Type,
  context: SignatureContext
)

case class SignatureContext(
  vars: Set[String],
  constraints: Map[String, Seq[Type]]
)

object SignatureContext {

  def empty: SignatureContext = {
    SignatureContext(Set.empty, Map.empty)
  }
}