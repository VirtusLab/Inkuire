package org.virtuslab.inkuire.plugin.transformers

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.model.TypeConstructor
import org.virtuslab.inkuire.model.*

class DefaultDokkaToSerializableModelTransformer : DokkaToSerializableModelTransformer() {
    override fun DRI.toSerializable(): SDRI = SDRI(
        packageName = this.packageName,
        className = this.classNames,
        callableName = this.callable?.let { it.name },
        original = this.toString()
    )
    override fun DClasslike.toSerializable() : SDClasslike {
        return when (this) {
            is DClass -> this.toSerializable()
            is DEnum -> this.toSerializable()
            is DObject -> this.toSerializable()
            is DAnnotation -> this.toSerializable()
            is DInterface -> this.toSerializable()
            else -> throw UnsupportedOperationException("Unsupported model class")
        }
    }

    override fun DTypeAlias.toSerializable() = SDTypeAlias(
        dri = dri.toSerializable(),
        name = name,
        type = type.toSerializable(),
        underlyingType = underlyingType.values.first().toSerializable()
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

    override fun DProperty.toSerializable() = SDProperty(
        dri = dri.toSerializable(),
        name = name,
        receiver = receiver?.toSerializable(),
        setter = setter?.toSerializable(),
        getter = getter?.toSerializable(),
        type = type.toSerializable()
    )


    override fun DAnnotation.toSerializable() = SDAnnotation(
        dri = dri.toSerializable(),
        name = name,
        functions = functions.map { it.toSerializable() },
        properties = properties.map { it.toSerializable() },
        classlikes = classlikes.mapNotNull { it.toSerializable() },
        companion = companion?.toSerializable(),
        constructors = constructors.map { it.toSerializable() }
    )

    override fun DObject.toSerializable() = SDObject(
        dri = dri.toSerializable(),
        name = name,
        functions = functions.map { it.toSerializable() },
        properties = properties.map { it.toSerializable() },
        classlikes = classlikes.mapNotNull { it.toSerializable() },
        supertypes = supertypes.values.toList().flatten().map { it.toString() }
    )

    override fun DInterface.toSerializable() = SDInterface(
        dri = dri.toSerializable(),
        name = name,
        functions = functions.map { it.toSerializable() },
        properties = properties.map { it.toSerializable() },
        classlikes = classlikes.map { it.toSerializable() },
        companion = companion?.toSerializable(),
        generics = generics.map { it.toSerializable() },
        supertypes = supertypes.values.toList().flatten().map { it.toString() }
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

    override fun DEnumEntry.toSerializable() = SDEnumEntry(
        dri = dri.toSerializable(),
        name = name,
        functions = functions.map { it.toSerializable() },
        properties = properties.map { it.toSerializable() },
        classlikes = classlikes.map { it.toSerializable() }
    )

    override fun DEnum.toSerializable() = SDEnum(
        dri = dri.toSerializable(),
        name = name,
        entries = entries.map { it.toSerializable() },
        constructors = constructors.map { it.toSerializable() },
        functions = functions.map { it.toSerializable() },
        properties = properties.map { it.toSerializable() },
        classlikes = classlikes.map { it.toSerializable() },
        companion = companion?.toSerializable(),
        supertypes = supertypes.values.toList().flatten().map { it.toString() }
    )

    override fun DClass.toSerializable() = SDClass(
        dri = dri.toSerializable(),
        name = name,
        constructors = constructors.map { it.toSerializable() },
        functions = functions.map { it.toSerializable() },
        properties = properties.map { it.toSerializable() },
        classlikes = classlikes.map { it.toSerializable() }.filterNotNull(),
        companion = companion?.toSerializable(),
        generics = generics.map { it.toSerializable() },
        supertypes = supertypes.values.toList().flatten().map { it.toString() }
    )

    override fun DPackage.toSerializable() = SDPackage(
        dri = dri.toSerializable(),
        name = name,
        functions = functions.map { it.toSerializable() },
        properties = properties.map { it.toSerializable() },
        classlikes = classlikes.map { it.toSerializable() },
        typealiases = typealiases.map { it.toSerializable() }
    )

    override fun DModule.toSerializable() = SDModule(
        dri = dri.toSerializable(),
        name = name,
        packages = packages.map { it.toSerializable() }
    )

    override fun Projection.toSerializable(): SProjection = when(this){
        is Bound -> this.toSerializable()
        is Star -> SStar
        is Variance -> SVariance(SVariance.Kind.valueOf(this.kind.name),this.inner.toSerializable())
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