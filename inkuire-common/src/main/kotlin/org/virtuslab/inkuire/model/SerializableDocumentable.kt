package org.virtuslab.inkuire.model

data class SDRI(
    val packageName: String?,
    val className: String?,
    val callableName: String?,
    val original: String
) {
    override fun toString(): String = original
}

data class SDModule(
    val dri: SDRI,
    val name: String,
    val packages: List<SDPackage>
)

data class SDPackage(
    val dri: SDRI,
    val name: String,
    val functions: List<SDFunction>,
    val properties: List<SDProperty>,
    val classlikes: List<SDClasslike>,
    val typealiases: List<SDTypeAlias>
)

data class SDClass(
    val dri: SDRI,
    val name: String,
    val constructors: List<SDFunction>,
    val functions: List<SDFunction>,
    val properties: List<SDProperty>,
    val classlikes: List<SDClasslike>,
    val companion: SDObject?,
    val generics: List<SDTypeParameter>,
    val supertypes: List<String>
) : SDClasslike()

data class SDEnum(
    val dri: SDRI,
    val name: String,
    val entries: List<SDEnumEntry>,
    val functions: List<SDFunction>,
    val properties: List<SDProperty>,
    val classlikes: List<SDClasslike>,
    val companion: SDObject?,
    val constructors: List<SDFunction>,
    val supertypes: List<String>
) : SDClasslike()

data class SDEnumEntry(
    val dri: SDRI,
    val name: String,
    val functions: List<SDFunction>,
    val properties: List<SDProperty>,
    val classlikes: List<SDClasslike>
)

data class SDFunction(
    val dri: SDRI,
    val name: String,
    val isConstructor: Boolean,
    val parameters: List<SDParameter>,
    val type: SBound,
    val generics: List<SDTypeParameter>,
    val receiver: SDParameter?
)

data class SDInterface(
    val dri: SDRI,
    val name: String,
    val functions: List<SDFunction>,
    val properties: List<SDProperty>,
    val classlikes: List<SDClasslike>,
    val companion: SDObject?,
    val generics: List<SDTypeParameter>,
    val supertypes: List<String>
) : SDClasslike()

data class SDObject(
    val name: String?,
    val dri: SDRI,
    val functions: List<SDFunction>,
    val properties: List<SDProperty>,
    val classlikes: List<SDClasslike>,
    val supertypes: List<String>
) : SDClasslike()

data class SDAnnotation(
    val name: String,
    val dri: SDRI,
    val functions: List<SDFunction>,
    val properties: List<SDProperty>,
    val classlikes: List<SDClasslike>,
    val companion: SDObject?,
    val constructors: List<SDFunction>
) : SDClasslike()

data class SDProperty(
    val dri: SDRI,
    val name: String,
    val type: SBound,
    val receiver: SDParameter?,
    val setter: SDFunction?,
    val getter: SDFunction?
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

data class SDTypeAlias(
    val dri: SDRI,
    val name: String,
    val type: SBound,
    val underlyingType: SBound
)

sealed class SDClasslike

//Placeholder for deserialization
class NullClasslike : SDClasslike()

class NullBound : SBound()

class NullProjection : SProjection()

sealed class SProjection

sealed class SBound : SProjection()

data class SOtherParameter(val name: String) : SBound()

object SStar : SProjection()

data class STypeConstructor(
        val dri: SDRI,
        val projections: List<SProjection>,
        val modifier: SFunctionModifiers = SFunctionModifiers.NONE
) : SBound()

enum class SFunctionModifiers {
    NONE, FUNCTION, EXTENSION
}

data class SNullable(val inner: SBound) : SBound()

data class SVariance(val kind: Kind, val inner: SBound) : SProjection() {
    enum class Kind { In, Out }
}

data class SPrimitiveJavaType(val name: String) : SBound()

object SVoid : SBound()

object SJavaObject : SBound()
