package org.virtuslab.inkuire.plugin.model

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.pages.PlatformData
import org.jetbrains.kotlin.utils.addToStdlib.cast

data class DModuleAdapter(
    val dri: DRI,
    val name: String,
    val packages: List<DPackageAdapter>,
    val platformData: List<PlatformData>,
    val extra: String
)
fun DModule.toAdapter() = DModuleAdapter(
    dri = dri,
    name = name,
    packages = packages.map { it.toAdapter() },
    platformData = platformData,
    extra = extra.toString()
)

data class DPackageAdapter(
    val dri: DRI,
    val name: String,
    val functions: List<DFunctionAdapter>,
    val properties: List<DPropertyAdapter>,
    val classlikes: List<DClasslikeAdapter>,
    val typealiases: List<DTypeAliasAdapter>,
    val platformData: List<PlatformData>,
    val extra: String
)
fun DPackage.toAdapter() = DPackageAdapter(
    dri = dri,
    name = name,
    functions = functions.map { it.toAdapter() },
    properties = properties.map { it.toAdapter() },
    classlikes = classlikes.mapNotNull { it.toClasslikeAdapter() },
    typealiases = typealiases.map { it.toAdapter() },
    platformData = platformData,
    extra = extra.toString()
)

data class DClassAdapter(
    val dri: DRI,
    val name: String,
    val constructors: List<DFunctionAdapter>,
    val functions: List<DFunctionAdapter>,
    val properties: List<DPropertyAdapter>,
    val classlikes: List<DClasslikeAdapter>,
    val visibility: PlatformDependent<Visibility>,
    val companion: DObjectAdapter?,
    val generics: List<DTypeParameterAdapter>,
    val supertypes: PlatformDependent<List<DRI>>,
    val modifier: PlatformDependent<Modifier>,
    val platformData: List<PlatformData>,
    val extra: String
) : DClasslikeAdapter()
fun DClass.toAdapter() = DClassAdapter(
    dri = dri,
    name = name,
    constructors = constructors.map { it.toAdapter() },
    functions = functions.map { it.toAdapter() },
    properties = properties.map { it.toAdapter() },
    classlikes = classlikes.map { it.toClasslikeAdapter() }.filterNotNull(),
    visibility = visibility,
    companion = companion?.toAdapter(),
    generics = generics.map { it.toAdapter() },
    supertypes = supertypes,
    modifier = modifier,
    platformData = platformData,
    extra = extra.toString()
)

data class DEnumAdapter(
    val dri: DRI,
    val name: String,
    val entries: List<DEnumEntryAdapter>,
    val functions: List<DFunctionAdapter>,
    val properties: List<DPropertyAdapter>,
    val classlikes: List<DClasslikeAdapter>,
    val visibility: PlatformDependent<Visibility>,
    val companion: DObjectAdapter?,
    val constructors: List<DFunctionAdapter>,
    val supertypes: PlatformDependent<List<DRI>>,
    val platformData: List<PlatformData>,
    val extra: String
) : DClasslikeAdapter()
fun DEnum.toAdapter() = DEnumAdapter(
    dri = dri,
    name = name,
    entries = entries.map { it.toAdapter() },
    constructors = constructors.map { it.toAdapter() },
    functions = functions.map { it.toAdapter() },
    properties = properties.map { it.toAdapter() },
    classlikes = classlikes.mapNotNull { it.toClasslikeAdapter() },
    visibility = visibility,
    companion = companion?.toAdapter(),
    supertypes = supertypes,
    platformData = platformData,
    extra = extra.toString()
)

data class DEnumEntryAdapter(
    val dri: DRI,
    val name: String,
    val functions: List<DFunctionAdapter>,
    val properties: List<DPropertyAdapter>,
    val classlikes: List<DClasslikeAdapter>,
    val platformData: List<PlatformData>,
    val extra: String
)
fun DEnumEntry.toAdapter() = DEnumEntryAdapter(
    dri = dri,
    name = name,
    functions = functions.map { it.toAdapter() },
    properties = properties.map { it.toAdapter() },
    classlikes = classlikes.mapNotNull { it.toClasslikeAdapter() },
    platformData = platformData,
    extra = extra.toString()
)

data class DFunctionAdapter(
    val dri: DRI,
    val name: String,
    val isConstructor: Boolean,
    val parameters: List<DParameterAdapter>,
    val visibility: PlatformDependent<Visibility>,
    val type: Bound,
    val generics: List<DTypeParameterAdapter>,
    val receiver: DParameter?,
    val modifier: PlatformDependent<Modifier>,
    val platformData: List<PlatformData>,
    val extra: String
)
fun DFunction.toAdapter() = DFunctionAdapter(
    dri = dri,
    name = name,
    isConstructor = isConstructor,
    parameters = parameters.map { it.toAdapter() },
    type = type,
    generics = generics.map { it.toAdapter() },
    receiver = receiver,
    modifier = modifier,
    visibility = visibility,
    platformData = platformData,
    extra = extra.toString()
)

data class DInterfaceAdapter(
    val dri: DRI,
    val name: String,
    val functions: List<DFunctionAdapter>,
    val properties: List<DPropertyAdapter>,
    val classlikes: List<DClasslikeAdapter>,
    val visibility: PlatformDependent<Visibility>,
    val companion: DObjectAdapter?,
    val generics: List<DTypeParameterAdapter>,
    val supertypes: PlatformDependent<List<DRI>>,
    val platformData: List<PlatformData>,
    val extra: String
) : DClasslikeAdapter()
fun DInterface.toAdapter() = DInterfaceAdapter(
    dri = dri,
    name = name,
    functions = functions.map { it.toAdapter() },
    properties = properties.map { it.toAdapter() },
    classlikes = classlikes.mapNotNull { it.cast<DClasslike>().toClasslikeAdapter() },
    visibility = visibility,
    companion = companion?.toAdapter(),
    generics = generics.map { it.toAdapter() },
    supertypes = supertypes,
    platformData = platformData,
    extra = extra.toString()
)

data class DObjectAdapter(
    val name: String?,
    val dri: DRI,
    val functions: List<DFunctionAdapter>,
    val properties: List<DPropertyAdapter>,
    val classlikes: List<DClasslikeAdapter>,
    val visibility: PlatformDependent<Visibility>,
    val supertypes: PlatformDependent<List<DRI>>,
    val platformData: List<PlatformData>,
    val extra: String
) : DClasslikeAdapter()
fun DObject.toAdapter() = DObjectAdapter(
    dri = dri,
    name = name,
    functions = functions.map { it.toAdapter() },
    properties = properties.map { it.toAdapter() },
    classlikes = classlikes.mapNotNull { it.toClasslikeAdapter() },
    visibility = visibility,
    supertypes = supertypes,
    platformData = platformData,
    extra = extra.toString()
)

data class DAnnotationAdapter(
    val name: String,
    val dri: DRI,
    val functions: List<DFunctionAdapter>,
    val properties: List<DPropertyAdapter>,
    val classlikes: List<DClasslikeAdapter>,
    val visibility: PlatformDependent<Visibility>,
    val companion: DObjectAdapter?,
    val constructors: List<DFunctionAdapter>,
    val platformData: List<PlatformData>,
    val extra: String
) : DClasslikeAdapter()
fun DAnnotation.toAdapter() = DAnnotationAdapter(
    dri = dri,
    name = name,
    functions = functions.map { it.toAdapter() },
    properties = properties.map { it.toAdapter() },
    classlikes = classlikes.mapNotNull { it.toClasslikeAdapter() },
    visibility = visibility,
    companion = companion?.toAdapter(),
    constructors = constructors.map { it.toAdapter() },
    platformData = platformData,
    extra = extra.toString()
)

data class DPropertyAdapter(
    val dri: DRI,
    val name: String,
    val visibility: PlatformDependent<Visibility>,
    val type: Bound,
    val receiver: DParameterAdapter?,
    val setter: DFunctionAdapter?,
    val getter: DFunctionAdapter?,
    val modifier: PlatformDependent<Modifier>,
    val platformData: List<PlatformData>,
    val extra: String
)
fun DProperty.toAdapter() = DPropertyAdapter(
    dri = dri,
    name = name,
    visibility = visibility,
    type = type,
    receiver = receiver?.toAdapter(),
    setter = setter?.toAdapter(),
    getter = getter?.toAdapter(),
    modifier = modifier,
    platformData = platformData,
    extra = extra.toString()
)

data class DParameterAdapter(
    val dri: DRI,
    val name: String?,
    val type: Bound,
    val platformData: List<PlatformData>,
    val extra: String
)
fun DParameter.toAdapter() = DParameterAdapter(
    dri = dri,
    name = name,
    type = type,
    platformData = platformData,
    extra = extra.toString()
)

data class DTypeParameterAdapter(
    val dri: DRI,
    val name: String,
    val bounds: List<Bound>,
    val platformData: List<PlatformData>,
    val extra: String
)
fun DTypeParameter.toAdapter() = DTypeParameterAdapter(
        dri = dri,
        name = name,
        bounds = bounds,
        platformData = platformData,
        extra = extra.toString()
)

data class DTypeAliasAdapter(
    val dri: DRI,
    val name: String,
    val type: Bound,
    val underlyingType: PlatformDependent<Bound>,
    val visibility: PlatformDependent<Visibility>,
    val platformData: List<PlatformData>,
    val extra: String
)
fun DTypeAlias.toAdapter() = DTypeAliasAdapter(
    dri = dri,
    name = name,
    type = type,
    underlyingType = underlyingType,
    visibility = visibility,
    platformData = platformData,
    extra = extra.toString()
)

sealed class DClasslikeAdapter
fun DClasslike.toClasslikeAdapter(): DClasslikeAdapter? {
    return when (this) {
        is DClass -> this.toAdapter()
        is DEnum -> this.toAdapter()
        is DObject -> this.toAdapter()
        is DAnnotation -> this.toAdapter()
        is DInterface -> this.toAdapter()
        else -> null
    }
}