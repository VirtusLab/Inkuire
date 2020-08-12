package org.virtuslab.inkuire.plugin.transformers

import com.intellij.psi.PsiMethod
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.analysis.DescriptorDocumentableSource
import org.jetbrains.dokka.analysis.PsiDocumentableSource
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.links.DriOfAny
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.model.TypeConstructor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue
import org.virtuslab.inkuire.model.*
import java.lang.IllegalStateException

object DefaultDokkaToSerializableModelTransformer : DokkaToSerializableModelTransformer() {

    private val uselessBound = Nullable(
            TypeConstructor(
                    DriOfAny,
                    emptyList()
            )
    )

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

    override fun DFunction.toSerializable(source: DokkaConfiguration.DokkaSourceSet) = SDFunction(
        dri = dri.toSerializable(),
        name = name,
        isConstructor = isConstructor,
        parameters = parameters.map { it.toSerializable() },
        areParametersDefault = this.alternativeParametersLists(source),
        generics = generics
                .map { p -> p.copy(
                        bounds = p.bounds.filter { it != uselessBound }
                )}
                .map { it.toSerializable() },
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
        is TypeParameter -> STypeParameter(declarationDRI.toSerializable(), name)
        is TypeConstructor -> STypeConstructor(
            dri.toSerializable(),
            projections.map { it.toSerializable() },
            modifier.toSerializable())
        is Nullable -> SNullable(inner.toSerializable())
        is PrimitiveJavaType -> SPrimitiveJavaType(name)
        is Void -> SVoid
        is JavaObject -> SJavaObject
        is Dynamic -> SDynamic
        is UnresolvedBound -> SUnresolvedBound(name)
    }

    private fun DFunction.alternativeParametersLists(source: DokkaConfiguration.DokkaSourceSet): List<Boolean> {
        return when(val elem = this.sources[source]) {
            is DescriptorDocumentableSource -> (elem.descriptor as FunctionDescriptor).valueParameters.map {
                it.hasDefaultValue()
            }
            is PsiDocumentableSource -> (elem.psi as PsiMethod).parameterList.parameters.map {
                false
            }
            else -> throw IllegalStateException("Unknown translator. Please provide custom implementation for obtaining default values.")
        }
    }
}