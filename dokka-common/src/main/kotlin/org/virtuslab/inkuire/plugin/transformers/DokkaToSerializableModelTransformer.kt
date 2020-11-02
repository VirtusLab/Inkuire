package org.virtuslab.inkuire.plugin.transformers

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.virtuslab.inkuire.kotlin.model.*

abstract class DokkaToSerializableModelTransformer {
    abstract fun DRI.toSerializable(): SDRI
    abstract fun DParameter.toSerializable(): SDParameter
    abstract fun DTypeParameter.toSerializable(): SDTypeParameter
    abstract fun Bound.toSerializable(): SBound
    abstract fun Projection.toSerializable(): SProjection
    abstract fun DFunction.toSerializable(source: DokkaConfiguration.DokkaSourceSet): SDFunction
}
