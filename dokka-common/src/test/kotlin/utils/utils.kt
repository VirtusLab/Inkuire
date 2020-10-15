package utils

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.model.properties.PropertyContainer

internal fun simpleFunction(
    dri: DRI,
    name: String,
    parameters: List<DParameter>,
    type: Bound,
    receiver: DParameter?,
    generics: List<DTypeParameter>
): DFunction = DFunction(
    dri = dri,
    name = name,
    isConstructor = false,
    parameters = parameters,
    documentation = emptyMap(),
    expectPresentInSet = null,
    sources = emptyMap(),
    visibility = emptyMap(),
    type = type,
    receiver = receiver,
    generics = generics,
    modifier = emptyMap(),
    sourceSets = emptySet(),
    extra = PropertyContainer.empty()
)

internal fun simpleParameter(
    dri: DRI,
    name: String?,
    type: Bound
): DParameter = DParameter(
    dri = dri,
    name = name,
    documentation = emptyMap(),
    expectPresentInSet = null,
    type = type,
    sourceSets = emptySet(),
    extra = PropertyContainer.empty()
)

internal fun simpleTypeParameter(
    variantTypeParameter: Variance<TypeParameter>,
    bounds: List<Bound>
): DTypeParameter = DTypeParameter(
    variantTypeParameter = variantTypeParameter,
    documentation = emptyMap(),
    expectPresentInSet = null,
    bounds = bounds,
    sourceSets = emptySet(),
    extra = PropertyContainer.empty()
)
