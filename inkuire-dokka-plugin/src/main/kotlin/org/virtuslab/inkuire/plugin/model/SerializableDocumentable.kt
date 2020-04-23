package org.virtuslab.inkuire.plugin.model

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.pages.PlatformData
import org.jetbrains.kotlin.utils.addToStdlib.cast

data class SDModule(
        val dri: DRI,
        val name: String,
        val packages: List<SDPackage>,
        val platformData: List<PlatformData>
)


data class SDPackage(
        val dri: DRI,
        val name: String,
        val functions: List<SDFunction>,
        val properties: List<SDProperty>,
        val classlikes: List<SDClasslike>,
        val typealiases: List<SDTypeAlias>,
        val platformData: List<PlatformData>
)

data class SDClass(
        val dri: DRI,
        val name: String,
        val constructors: List<SDFunction>,
        val functions: List<SDFunction>,
        val properties: List<SDProperty>,
        val classlikes: List<SDClasslike>,
        val visibility: PlatformDependent<Visibility>,
        val companion: SDObject?,
        val generics: List<SDTypeParameter>,
        val supertypes: PlatformDependent<List<DRI>>,
        val modifier: PlatformDependent<Modifier>,
        val platformData: List<PlatformData>
) : SDClasslike()

data class SDEnum(
        val dri: DRI,
        val name: String,
        val entries: List<SDEnumEntry>,
        val functions: List<SDFunction>,
        val properties: List<SDProperty>,
        val classlikes: List<SDClasslike>,
        val visibility: PlatformDependent<Visibility>,
        val companion: SDObject?,
        val constructors: List<SDFunction>,
        val supertypes: PlatformDependent<List<DRI>>,
        val platformData: List<PlatformData>
) : SDClasslike()

data class SDEnumEntry(
        val dri: DRI,
        val name: String,
        val functions: List<SDFunction>,
        val properties: List<SDProperty>,
        val classlikes: List<SDClasslike>,
        val platformData: List<PlatformData>
)

data class SDFunction(
        val dri: DRI,
        val name: String,
        val isConstructor: Boolean,
        val parameters: List<SDParameter>,
        val visibility: PlatformDependent<Visibility>,
        val type: Bound,
        val generics: List<SDTypeParameter>,
        val receiver: DParameter?,
        val modifier: PlatformDependent<Modifier>,
        val platformData: List<PlatformData>
)

data class SDInterface(
        val dri: DRI,
        val name: String,
        val functions: List<SDFunction>,
        val properties: List<SDProperty>,
        val classlikes: List<SDClasslike>,
        val visibility: PlatformDependent<Visibility>,
        val companion: SDObject?,
        val generics: List<SDTypeParameter>,
        val supertypes: PlatformDependent<List<DRI>>,
        val platformData: List<PlatformData>
) : SDClasslike()

data class SDObject(
        val name: String?,
        val dri: DRI,
        val functions: List<SDFunction>,
        val properties: List<SDProperty>,
        val classlikes: List<SDClasslike>,
        val visibility: PlatformDependent<Visibility>,
        val supertypes: PlatformDependent<List<DRI>>,
        val platformData: List<PlatformData>
) : SDClasslike()

data class SDAnnotation(
        val name: String,
        val dri: DRI,
        val functions: List<SDFunction>,
        val properties: List<SDProperty>,
        val classlikes: List<SDClasslike>,
        val visibility: PlatformDependent<Visibility>,
        val companion: SDObject?,
        val constructors: List<SDFunction>,
        val platformData: List<PlatformData>
) : SDClasslike()


data class SDProperty(
        val dri: DRI,
        val name: String,
        val visibility: PlatformDependent<Visibility>,
        val type: Bound,
        val receiver: SDParameter?,
        val setter: SDFunction?,
        val getter: SDFunction?,
        val modifier: PlatformDependent<Modifier>,
        val platformData: List<PlatformData>
)

data class SDParameter(
    val dri: DRI,
    val name: String?,
    val type: Bound,
    val platformData: List<PlatformData>
)

data class SDTypeParameter(
    val dri: DRI,
    val name: String,
    val bounds: List<Bound>,
    val platformData: List<PlatformData>
)

data class SDTypeAlias(
    val dri: DRI,
    val name: String,
    val type: Bound,
    val underlyingType: PlatformDependent<Bound>,
    val visibility: PlatformDependent<Visibility>,
    val platformData: List<PlatformData>
)


sealed class SDClasslike
