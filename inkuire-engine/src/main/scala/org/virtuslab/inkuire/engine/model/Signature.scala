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
) {
  // TODO: Issue #28
  override def equals(obj: Any): Boolean = obj match {
    case other: SignatureContext if this.vars.size == other.vars.size => true
    case _ => false
  }
}

object SignatureContext {

  def empty: SignatureContext = {
    SignatureContext(Set.empty, Map.empty)
  }
}