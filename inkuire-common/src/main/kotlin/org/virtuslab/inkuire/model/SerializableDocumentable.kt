package org.virtuslab.inkuire.model

data class SDRI(
    val packageName: String?,
    val className: String?,
    val callableName: String?,
    val original: String
) {
    override fun toString(): String = original
}

data class SDFunction(
    val dri: SDRI,
    val name: String,
    val isConstructor: Boolean,
    val parameters: List<SDParameter>,
    val areParametersDefault: List<Boolean>,
    val type: SBound,
    val generics: List<SDTypeParameter>,
    val receiver: SDParameter?
)

data class SDParameter(
    val dri: SDRI,
    val name: String?,
    val type: SBound
)

data class SDTypeParameter(
    val dri: SDRI,
    val name: String,
    val bounds: List<SBound>
)

data class AncestryGraph(
    val dri: SDRI,
    val type: SBound,
    val projections: List<SProjection>
)







