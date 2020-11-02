package org.virtuslab.inkuire.plugin.transformers

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.virtuslab.inkuire.kotlin.model.AncestryGraph
import org.virtuslab.inkuire.kotlin.model.STypeConstructor
import org.virtuslab.inkuire.plugin.transformers.DefaultDokkaToSerializableModelTransformer.toSerializable

fun List<AncestryGraph>.anyAndNothingAppender(): List<AncestryGraph> = map {
    if (it.superTypes.isEmpty())
        it.copy(superTypes = listOf(STypeConstructor(DRI("kotlin", "Any").toSerializable(), emptyList())))
    else
        it
} // TODO: Add Nothing handling

fun List<DFunction>.javaPrimitivesMapper(): List<DFunction> = map {
    it.copy(
        parameters = it.parameters.map(::mapIfPrimitive),
        type = it.type.let(::mapIfPrimitive),
        receiver = it.receiver?.let(::mapIfPrimitive) // Should never succeed actually
    )
}

fun List<DFunction>.functionalTypesNormalizerTransformer(): List<DFunction> = map {
    it.copy(
        parameters = it.parameters.map(::mapIfFunctionalType),
        type = it.type.let(::mapIfFunctionalType),
        generics = it.generics.map(::mapIfFunctionalType),
        receiver = it.receiver?.let(::mapIfFunctionalType)
    )
}

private val mappings: Map<Bound, Bound> = mapOf(
    PrimitiveJavaType("int") to GenericTypeConstructor(DRI("kotlin", "Int"), emptyList()),
    PrimitiveJavaType("boolean") to GenericTypeConstructor(DRI("kotlin", "Boolean"), emptyList()),
    PrimitiveJavaType("byte") to GenericTypeConstructor(DRI("kotlin", "Byte"), emptyList()),
    PrimitiveJavaType("char") to GenericTypeConstructor(DRI("kotlin", "Char"), emptyList()),
    PrimitiveJavaType("short") to GenericTypeConstructor(DRI("kotlin", "Short"), emptyList()),
    PrimitiveJavaType("long") to GenericTypeConstructor(DRI("kotlin", "Long"), emptyList()),
    PrimitiveJavaType("float") to GenericTypeConstructor(DRI("kotlin", "Float"), emptyList()),
    PrimitiveJavaType("double") to GenericTypeConstructor(DRI("kotlin", "Double"), emptyList()),
    Void to GenericTypeConstructor(DRI("kotlin", "Unit"), emptyList()),
    JavaObject to GenericTypeConstructor(DRI("kotlin", "Any"), emptyList())
)

private fun mapIfPrimitive(bound: Bound): Bound = mappings[bound] ?: bound

private fun mapIfPrimitive(parameter: DParameter): DParameter = parameter.copy(type = parameter.type.let(::mapIfPrimitive))

private fun mapIfFunctionalType(projection: Projection): Projection = if (projection is Bound)
    mapIfFunctionalType(projection)
else
    projection

private fun mapIfFunctionalType(bound: Bound): Bound = if (bound is FunctionalTypeConstructor)
    bound.copy(dri = bound.dri.copy(packageName = "kotlin"), projections = bound.projections.map(::mapIfFunctionalType))
else
    bound

private fun mapIfFunctionalType(parameter: DParameter): DParameter = parameter.copy(type = parameter.type.let(::mapIfFunctionalType))

private fun mapIfFunctionalType(parameter: DTypeParameter): DTypeParameter = parameter.copy(bounds = parameter.bounds.map(::mapIfFunctionalType))
