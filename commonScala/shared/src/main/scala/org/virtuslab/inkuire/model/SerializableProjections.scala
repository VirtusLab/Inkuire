package org.virtuslab.inkuire.model

sealed class SProjection

object SStar extends SProjection()

sealed abstract class SVariance extends SProjection() {
  val inner: SBound
}

case class SCovariance(override val inner: SBound) extends SVariance() {
  override def toString = "out"
}

case class SContravariance(override val inner: SBound) extends SVariance() {
  override def toString = "in"
}

case class SInvariance(override val inner: SBound) extends SVariance() {
  override def toString = ""
}

sealed class SBound extends SProjection()

object SVoid extends SBound()

object SJavaObject extends SBound()

object SDynamic extends SBound()

case class SUnresolvedBound(name: String) extends SBound()

case class STypeParameter(dri: SDRI, name: String) extends SBound()

case class SNullable(inner: SBound) extends SBound()

case class SPrimitiveJavaType(name: String) extends SBound()

case class STypeConstructor(
  dri:         SDRI,
  projections: List[SProjection],
  modifier:    SFunctionModifiers = SFunctionModifiers.NONE
) extends SBound()

sealed class SFunctionModifiers

object SFunctionModifiers {
  object NONE extends SFunctionModifiers
  object FUNCTION extends SFunctionModifiers
  object EXTENSION extends SFunctionModifiers
}
