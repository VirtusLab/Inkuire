package org.virtuslab.inkuire.model

sealed class SProjection

object SStar : SProjection()

data class SVariance(val kind: Kind, val inner: SBound) : SProjection() {
    enum class Kind { In, Out }
}

sealed class SBound : SProjection()

object SVoid : SBound()

object SJavaObject : SBound()

object SDynamic : SBound()

data class SUnresolvedBound(val name: String) : SBound()

data class STypeParameter(val dri: SDRI, val name: String) : SBound()

data class SNullable(val inner: SBound) : SBound()

data class SPrimitiveJavaType(val name: String) : SBound()

data class STypeConstructor(
    val dri: SDRI,
    val projections: List<SProjection>,
    val modifier: SFunctionModifiers = SFunctionModifiers.NONE
) : SBound()

enum class SFunctionModifiers {
    NONE, FUNCTION, EXTENSION
}