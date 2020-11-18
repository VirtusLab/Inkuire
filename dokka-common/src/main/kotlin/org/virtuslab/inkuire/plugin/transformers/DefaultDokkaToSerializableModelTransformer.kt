package org.virtuslab.inkuire.plugin.transformers

import com.intellij.psi.PsiMethod
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.analysis.DescriptorDocumentableSource
import org.jetbrains.dokka.analysis.PsiDocumentableSource
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.links.DriOfAny
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.pages.LocationResolver
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue
import org.virtuslab.inkuire.kotlin.model.*

object DefaultDokkaToSerializableModelTransformer : DokkaToSerializableModelTransformer() {

    private val uselessBound = Nullable(
        GenericTypeConstructor(
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
        variantTypeParameter = variantTypeParameter.toSerializable() as SVariance<*>,
        bounds = bounds.map { it.toSerializable() }
    )

    override fun DParameter.toSerializable() = SDParameter(
        dri = dri.toSerializable(),
        name = name,
        type = type.toSerializable()
    )

    override fun DFunction.toSerializable(source: DokkaConfiguration.DokkaSourceSet, locationResolver: LocationResolver?) = SDFunction(
        dri = dri.toSerializable(),
        name = name,
        isConstructor = isConstructor,
        parameters = parameters.map { it.toSerializable() },
        areParametersDefault = this.alternativeParametersLists(source),
        generics = generics
            .map { p ->
                p.copy(
                    bounds = p.bounds.filter { it != uselessBound }
                )
            }
            .map { it.toSerializable() },
        receiver = receiver?.toSerializable(),
        type = type.toSerializable(),
        location = try { locationResolver?.let { it(dri, setOf(source.toDisplaySourceSet())) } ?: dri.packageName.toString() } catch (e: Exception) {
            ""
        }
    )

    override fun Projection.toSerializable(): SProjection = when (this) {
        is Bound -> this.toSerializable()
        is Star -> SStar
        is Variance<*> -> when (this) {
            is Covariance<*> -> SCovariance(inner.toSerializable())
            is Contravariance<*> -> SContravariance(inner.toSerializable())
            is Invariance<*> -> SInvariance(inner.toSerializable())
        }
    }

    override fun Bound.toSerializable(): SBound = when (this) {
        is TypeParameter -> STypeParameter(dri.toSerializable(), name)
        is GenericTypeConstructor -> STypeConstructor(
            dri.toSerializable(),
            projections.map { it.toSerializable() },
            SFunctionModifiers.NONE
        )
        is FunctionalTypeConstructor -> STypeConstructor(
            dri.toSerializable(),
            projections.map { it.toSerializable() },
            SFunctionModifiers.EXTENSION.takeIf { isExtensionFunction } ?: SFunctionModifiers.FUNCTION
        )
        is Nullable -> SNullable(inner.toSerializable())
        is PrimitiveJavaType -> SPrimitiveJavaType(name)
        is Void -> SVoid
        is JavaObject -> SJavaObject
        is Dynamic -> SDynamic
        is UnresolvedBound -> SUnresolvedBound(name)
        is TypeAliased -> typeAlias.toSerializable()
    }

    private fun DFunction.alternativeParametersLists(source: DokkaConfiguration.DokkaSourceSet): List<Boolean> {
        return when (val elem = this.sources[source]) {
            is DescriptorDocumentableSource -> (elem.descriptor as FunctionDescriptor).valueParameters.map {
                it.hasDefaultValue()
            }
            is PsiDocumentableSource -> (elem.psi as PsiMethod).parameterList.parameters.map {
                false
            }
            else -> this.parameters.map { false }.also {
                println("Unknown translator. Please provide custom implementation for obtaining default values.")
            }
        }
    }
}
