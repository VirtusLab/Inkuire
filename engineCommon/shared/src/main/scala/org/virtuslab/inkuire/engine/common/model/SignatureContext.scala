package org.virtuslab.inkuire.engine.common.model

import cats.kernel.Monoid

case class SignatureContext(
  vars:        Set[String],
  constraints: Map[String, Seq[TypeLike]]
) {
  override def equals(obj: Any): Boolean =
    obj match {
      case other: SignatureContext if this.vars.size == other.vars.size => true
      case _ => false
    }
}

object SignatureContext {

  def empty: SignatureContext = Monoid[SignatureContext].empty

  implicit val signatureContextMonoid: Monoid[SignatureContext] = new Monoid[SignatureContext] {
    override def empty: SignatureContext = SignatureContext(Set.empty, Map.empty)

    override def combine(x: SignatureContext, y: SignatureContext): SignatureContext = {
      SignatureContext(
        x.vars ++ y.vars,
        x.constraints ++ y.constraints
      )
    }
  }
}
