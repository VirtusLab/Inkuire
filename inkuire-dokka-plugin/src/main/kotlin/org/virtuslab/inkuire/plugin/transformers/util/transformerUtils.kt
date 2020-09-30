package org.virtuslab.inkuire.plugin.transformers.util

import org.jetbrains.dokka.model.*
import java.lang.IllegalArgumentException

internal fun Documentable.transformDocumentables(dFunc: (Documentable) -> Documentable, pFunc: (Projection) -> Projection): Documentable {
    return dFunc(when (this) {
        is DModule -> copy(packages = packages.map { it.transformDocumentables(dFunc, pFunc) as DPackage })
        is DPackage -> copy(
                classlikes = classlikes.map { it.transformDocumentables(dFunc, pFunc) as DClasslike },
                functions = functions.map { it.transformDocumentables(dFunc, pFunc) as DFunction }, 
                properties = properties.map { it.transformDocumentables(dFunc, pFunc) as DProperty },
                typealiases = typealiases.map { it.transformDocumentables(dFunc, pFunc) as DTypeAlias }
        )
        is DClass -> copy(
                classlikes = classlikes.map { it.transformDocumentables(dFunc, pFunc) as DClasslike },
                functions = functions.map { it.transformDocumentables(dFunc, pFunc) as DFunction },
                properties = properties.map { it.transformDocumentables(dFunc, pFunc) as DProperty },
                constructors = constructors.map { it.transformDocumentables(dFunc, pFunc) as DFunction },
                generics = generics.map { it.transformDocumentables(dFunc, pFunc) as DTypeParameter }
        )
        is DObject -> copy(
                classlikes = classlikes.map { it.transformDocumentables(dFunc, pFunc) as DClasslike },
                functions = functions.map { it.transformDocumentables(dFunc, pFunc) as DFunction },
                properties = properties.map { it.transformDocumentables(dFunc, pFunc) as DProperty }
        )
        is DAnnotation -> copy(
                classlikes = classlikes.map { it.transformDocumentables(dFunc, pFunc) as DClasslike },
                functions = functions.map { it.transformDocumentables(dFunc, pFunc) as DFunction },
                properties = properties.map { it.transformDocumentables(dFunc, pFunc) as DProperty },
                constructors = constructors.map { it.transformDocumentables(dFunc, pFunc) as DFunction },
                generics = generics.map { it.transformDocumentables(dFunc, pFunc) as DTypeParameter }
        )
        is DInterface -> copy(
                classlikes = classlikes.map { it.transformDocumentables(dFunc, pFunc) as DClasslike },
                functions = functions.map { it.transformDocumentables(dFunc, pFunc) as DFunction },
                properties = properties.map { it.transformDocumentables(dFunc, pFunc) as DProperty },
                generics = generics.map { it.transformDocumentables(dFunc, pFunc) as DTypeParameter }
        )
        is DEnum -> copy(
                classlikes = classlikes.map { it.transformDocumentables(dFunc, pFunc) as DClasslike },
                functions = functions.map { it.transformDocumentables(dFunc, pFunc) as DFunction },
                properties = properties.map { it.transformDocumentables(dFunc, pFunc) as DProperty },
                constructors = constructors.map { it.transformDocumentables(dFunc, pFunc) as DFunction }
        )
        is DEnumEntry -> copy(
                classlikes = classlikes.map { it.transformDocumentables(dFunc, pFunc) as DClasslike },
                functions = functions.map { it.transformDocumentables(dFunc, pFunc) as DFunction },
                properties = properties.map { it.transformDocumentables(dFunc, pFunc) as DProperty }
        )
        is DTypeAlias -> copy(
                generics = generics.map { it.transformDocumentables(dFunc, pFunc) as DTypeParameter }
        )
        is DParameter -> copy(

        )
        is DFunction -> copy(
                parameters = parameters.map { it.transformDocumentables(dFunc, pFunc) as DParameter },
                generics = generics.map { it.transformDocumentables(dFunc, pFunc) as DTypeParameter }
        )
        is DProperty -> copy(
                getter = getter?.let { it.transformDocumentables(dFunc, pFunc) as DFunction },
                setter = setter?.let { it.transformDocumentables(dFunc, pFunc) as DFunction },
                generics = generics.map { it.transformDocumentables(dFunc, pFunc) as DTypeParameter }
        )
        is DTypeParameter -> copy(
                
        )
        else -> throw IllegalArgumentException("Not supported documentable while transforming documentables tree")
    })
}
