package org.virtuslab.inkuire.plugin.transformers

import org.jetbrains.dokka.model.*
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.virtuslab.inkuire.plugin.model.*

class DefaultDokkaToSerializableModelTransformer : DokkaToSerializableModelTransformer() {
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
            dri = dri,
            name = name,
            type = type,
            underlyingType = underlyingType,
            visibility = visibility,
            platformData = platformData
    )

    override fun DTypeParameter.toSerializable() = SDTypeParameter(
            dri = dri,
            name = name,
            bounds = bounds,
            platformData = platformData
    )

    override fun DParameter.toSerializable() = SDParameter(
            dri = dri,
            name = name,
            type = type,
            platformData = platformData
    )

    override fun DProperty.toSerializable() = SDProperty(
            dri = dri,
            name = name,
            visibility = visibility,
            type = type,
            receiver = receiver?.toSerializable(),
            setter = setter?.toSerializable(),
            getter = getter?.toSerializable(),
            modifier = modifier,
            platformData = platformData
    )


    override fun DAnnotation.toSerializable() = SDAnnotation(
            dri = dri,
            name = name,
            functions = functions.map { it.toSerializable() },
            properties = properties.map { it.toSerializable() },
            classlikes = classlikes.mapNotNull { it.toSerializable() },
            visibility = visibility,
            companion = companion?.toSerializable(),
            constructors = constructors.map { it.toSerializable() },
            platformData = platformData
    )

    override fun DObject.toSerializable() = SDObject(
            dri = dri,
            name = name,
            functions = functions.map { it.toSerializable() },
            properties = properties.map { it.toSerializable() },
            classlikes = classlikes.mapNotNull { it.toSerializable() },
            visibility = visibility,
            supertypes = supertypes,
            platformData = platformData
    )

    override fun DInterface.toSerializable() = SDInterface(
            dri = dri,
            name = name,
            functions = functions.map { it.toSerializable() },
            properties = properties.map { it.toSerializable() },
            classlikes = classlikes.map { it.toSerializable() },
            visibility = visibility,
            companion = companion?.toSerializable(),
            generics = generics.map { it.toSerializable() },
            supertypes = supertypes,
            platformData = platformData
    )

    override fun DFunction.toSerializable() = SDFunction(
            dri = dri,
            name = name,
            isConstructor = isConstructor,
            parameters = parameters.map { it.toSerializable() },
            type = type,
            generics = generics.map { it.toSerializable() },
            receiver = receiver,
            modifier = modifier,
            visibility = visibility,
            platformData = platformData
    )

    override fun DEnumEntry.toSerializable() = SDEnumEntry(
            dri = dri,
            name = name,
            functions = functions.map { it.toSerializable() },
            properties = properties.map { it.toSerializable() },
            classlikes = classlikes.map { it.toSerializable() },
            platformData = platformData
    )

    override fun DEnum.toSerializable() = SDEnum(
            dri = dri,
            name = name,
            entries = entries.map { it.toSerializable() },
            constructors = constructors.map { it.toSerializable() },
            functions = functions.map { it.toSerializable() },
            properties = properties.map { it.toSerializable() },
            classlikes = classlikes.map { it.toSerializable() },
            visibility = visibility,
            companion = companion?.toSerializable(),
            supertypes = supertypes,
            platformData = platformData
    )

    override fun DClass.toSerializable() = SDClass(
            dri = dri,
            name = name,
            constructors = constructors.map { it.toSerializable() },
            functions = functions.map { it.toSerializable() },
            properties = properties.map { it.toSerializable() },
            classlikes = classlikes.map { it.toSerializable() }.filterNotNull(),
            visibility = visibility,
            companion = companion?.toSerializable(),
            generics = generics.map { it.toSerializable() },
            supertypes = supertypes,
            modifier = modifier,
            platformData = platformData
    )

    override fun DPackage.toSerializable() = SDPackage(
            dri = dri,
            name = name,
            functions = functions.map { it.toSerializable() },
            properties = properties.map { it.toSerializable() },
            classlikes = classlikes.map { it.toSerializable() },
            typealiases = typealiases.map { it.toSerializable() },
            platformData = platformData
    )

    override fun DModule.toSerializable() = SDModule(
            dri = dri,
            name = name,
            packages = packages.map { it.toSerializable() },
            platformData = platformData
    )

}