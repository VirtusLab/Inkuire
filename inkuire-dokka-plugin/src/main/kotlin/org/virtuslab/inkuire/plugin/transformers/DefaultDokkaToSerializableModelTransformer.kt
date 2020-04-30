package org.virtuslab.inkuire.plugin.transformers

import org.jetbrains.dokka.model.*
import org.virtuslab.inkuire.model.*

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
        dri = dri.toString(),
        name = name
    )

    override fun DTypeParameter.toSerializable() = SDTypeParameter(
        dri = dri.toString(),
        name = name
    )

    override fun DParameter.toSerializable() = SDParameter(
        dri = dri.toString(),
        name = name
    )

    override fun DProperty.toSerializable() = SDProperty(
        dri = dri.toString(),
        name = name,
        receiver = receiver?.toSerializable(),
        setter = setter?.toSerializable(),
        getter = getter?.toSerializable()
    )


    override fun DAnnotation.toSerializable() = SDAnnotation(
        dri = dri.toString(),
        name = name,
        functions = functions.map { it.toSerializable() },
        properties = properties.map { it.toSerializable() },
        classlikes = classlikes.mapNotNull { it.toSerializable() },
        companion = companion?.toSerializable(),
        constructors = constructors.map { it.toSerializable() }
    )

    override fun DObject.toSerializable() = SDObject(
        dri = dri.toString(),
        name = name,
        functions = functions.map { it.toSerializable() },
        properties = properties.map { it.toSerializable() },
        classlikes = classlikes.mapNotNull { it.toSerializable() },
        supertypes = supertypes.allValues.toList().flatten().map { it.toString() }
    )

    override fun DInterface.toSerializable() = SDInterface(
        dri = dri.toString(),
        name = name,
        functions = functions.map { it.toSerializable() },
        properties = properties.map { it.toSerializable() },
        classlikes = classlikes.map { it.toSerializable() },
        companion = companion?.toSerializable(),
        generics = generics.map { it.toSerializable() },
        supertypes = supertypes.allValues.toList().flatten().map { it.toString() }
    )

    override fun DFunction.toSerializable() = SDFunction(
        dri = dri.toString(),
        name = name,
        isConstructor = isConstructor,
        parameters = parameters.map { it.toSerializable() },
        generics = generics.map { it.toSerializable() },
        receiver = receiver?.toSerializable()
    )

    override fun DEnumEntry.toSerializable() = SDEnumEntry(
        dri = dri.toString(),
        name = name,
        functions = functions.map { it.toSerializable() },
        properties = properties.map { it.toSerializable() },
        classlikes = classlikes.map { it.toSerializable() }
    )

    override fun DEnum.toSerializable() = SDEnum(
        dri = dri.toString(),
        name = name,
        entries = entries.map { it.toSerializable() },
        constructors = constructors.map { it.toSerializable() },
        functions = functions.map { it.toSerializable() },
        properties = properties.map { it.toSerializable() },
        classlikes = classlikes.map { it.toSerializable() },
        companion = companion?.toSerializable(),
        supertypes = supertypes.allValues.toList().flatten().map { it.toString() }
    )

    override fun DClass.toSerializable() = SDClass(
        dri = dri.toString(),
        name = name,
        constructors = constructors.map { it.toSerializable() },
        functions = functions.map { it.toSerializable() },
        properties = properties.map { it.toSerializable() },
        classlikes = classlikes.map { it.toSerializable() }.filterNotNull(),
        companion = companion?.toSerializable(),
        generics = generics.map { it.toSerializable() },
        supertypes = supertypes.allValues.toList().flatten().map { it.toString() }
    )

    override fun DPackage.toSerializable() = SDPackage(
        dri = dri.toString(),
        name = name,
        functions = functions.map { it.toSerializable() },
        properties = properties.map { it.toSerializable() },
        classlikes = classlikes.map { it.toSerializable() },
        typealiases = typealiases.map { it.toSerializable() }
    )

    override fun DModule.toSerializable() = SDModule(
        dri = dri.toString(),
        name = name,
        packages = packages.map { it.toSerializable() }
    )

}