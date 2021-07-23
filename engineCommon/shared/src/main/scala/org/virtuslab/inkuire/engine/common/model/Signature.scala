package org.virtuslab.inkuire.engine.common.model

case class Signature(
  receiver:  Option[Contravariance],
  arguments: Seq[Contravariance],
  result:    Covariance,
  context:   SignatureContext
) {
  def typesWithVariances: Seq[Variance] = receiver.toSeq ++ arguments :+ result
}

object Signature {
  def apply(receiver: Option[TypeLike], arguments: Seq[TypeLike], result: TypeLike, context: SignatureContext): Signature =
    Signature(receiver.map(Contravariance), arguments.map(Contravariance), Covariance(result), context)
}
