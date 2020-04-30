package org.virtuslab.inkuire.model

data class SDModule(
    val dri: String,
    val name: String,
    val packages: List<SDPackage>
)

data class SDPackage(
    val dri: String,
    val name: String,
    val functions: List<SDFunction>,
    val properties: List<SDProperty>,
    val classlikes: List<SDClasslike>,
    val typealiases: List<SDTypeAlias>
)

data class SDClass(
    val dri: String,
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
    val dri: String,
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
    val dri: String,
    val name: String,
    val functions: List<SDFunction>,
    val properties: List<SDProperty>,
    val classlikes: List<SDClasslike>
)

data class SDFunction(
    val dri: String,
    val name: String,
    val isConstructor: Boolean,
    val parameters: List<SDParameter>,
//    val type: Bound,
    val generics: List<SDTypeParameter>,
    val receiver: SDParameter?
)

data class SDInterface(
    val dri: String,
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
    val dri: String,
    val functions: List<SDFunction>,
    val properties: List<SDProperty>,
    val classlikes: List<SDClasslike>,
    val supertypes: List<String>
) : SDClasslike()

data class SDAnnotation(
    val name: String,
    val dri: String,
    val functions: List<SDFunction>,
    val properties: List<SDProperty>,
    val classlikes: List<SDClasslike>,
    val companion: SDObject?,
    val constructors: List<SDFunction>
) : SDClasslike()

data class SDProperty(
    val dri: String,
    val name: String,
//    val type: Bound,
    val receiver: SDParameter?,
    val setter: SDFunction?,
    val getter: SDFunction?
)

data class SDParameter(
    val dri: String,
    val name: String?
//    val type: Bound,
)

data class SDTypeParameter(
    val dri: String,
    val name: String
//    val bounds: List<Bound>
)

data class SDTypeAlias(
    val dri: String,
    val name: String
//    val type: Bound,
//    val underlyingType: Bound
)

sealed class SDClasslike
