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
  override def equals(obj: Any): Boolean = obj match {
    case other: SignatureContext if this.vars.size == other.vars.size =>
      val varMap = this.vars.zip(other.vars).toMap
      this.vars.forall(key => this.constraints(key) == other.constraints(varMap(key)))
    case _ => false
  }
}

object SignatureContext {

  def empty: SignatureContext = {
    SignatureContext(Set.empty, Map.empty)
  }
}