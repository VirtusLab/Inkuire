package org.virtuslab.inkuire.engine.model

case class Signature(
  receiver:  Option[Contravariance],
  arguments: Seq[Contravariance],
  result:    Covariance,
  context:   SignatureContext
) {
  def typesWithVariances: Seq[Variance] = receiver.toSeq ++ arguments ++ Seq(result)
}

object Signature {
  def apply(receiver: Option[Type], arguments: Seq[Type], result: Type, context: SignatureContext): Signature =
    Signature(receiver.map(Contravariance), arguments.map(Contravariance), Covariance(result), context)
}
