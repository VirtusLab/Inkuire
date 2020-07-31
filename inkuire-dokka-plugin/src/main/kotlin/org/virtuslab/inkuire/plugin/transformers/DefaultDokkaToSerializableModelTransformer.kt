package org.virtuslab.inkuire.plugin.transformers

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.model.TypeConstructor
import org.virtuslab.inkuire.model.*

object DefaultDokkaToSerializableModelTransformer : DokkaToSerializableModelTransformer() {

    override fun DRI.toSerializable(): SDRI = SDRI(
        packageName = this.packageName,
        className = this.classNames,
        callableName = this.callable?.let { it.name },
        original = this.toString()
    )

    override fun DTypeParameter.toSerializable() = SDTypeParameter(
        dri = dri.toSerializable(),
        name = name,
        bounds = bounds.map { it.toSerializable() }
    )

    override fun DParameter.toSerializable() = SDParameter(
        dri = dri.toSerializable(),
        name = name,
        type = type.toSerializable()
    )

    override fun DFunction.toSerializable() = SDFunction(
        dri = dri.toSerializable(),
        name = name,
        isConstructor = isConstructor,
        parameters = parameters.map { it.toSerializable() },
        generics = generics.map { it.toSerializable() },
        receiver = receiver?.toSerializable(),
        type = type.toSerializable()
    )

    override fun Projection.toSerializable(): SProjection = when(this) {
        is Bound -> this.toSerializable()
        is Star -> SStar
        is Variance -> SVariance(SVariance.Kind.valueOf(this.kind.name), this.inner.toSerializable())
    }

    override fun FunctionModifiers.toSerializable(): SFunctionModifiers = SFunctionModifiers.valueOf(this.name)

    override fun Bound.toSerializable() : SBound = when(this) {
        is OtherParameter -> SOtherParameter(this.name)
        is TypeConstructor -> STypeConstructor(
            dri.toSerializable(),
            projections.map { it.toSerializable() },
            modifier.toSerializable())
        is Nullable -> SNullable(this.inner.toSerializable())
        is PrimitiveJavaType -> SPrimitiveJavaType(this.name)
        is Void -> SVoid
        is JavaObject -> SJavaObject
        is Dynamic -> SDynamic
        is UnresolvedBound -> SUnresolvedBound
    }

}