package org.virtuslab.inkuire.model

sealed class SProjection

object SStar : SProjection()

sealed class SVariance<out T : SBound> : SProjection() {
    abstract val inner: T
}

data class SCovariance<out T : SBound>(override val inner: T) : SVariance<T>() {
    override fun toString() = "out"
}

data class SContravariance<out T : SBound>(override val inner: T) : SVariance<T>() {
    override fun toString() = "in"
}

data class SInvariance<out T : SBound>(override val inner: T) : SVariance<T>() {
    override fun toString() = ""
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
