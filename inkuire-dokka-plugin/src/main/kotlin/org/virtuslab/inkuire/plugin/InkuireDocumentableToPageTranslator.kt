package org.virtuslab.inkuire.plugin

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.transformers.documentation.DocumentableToPageTranslator
import org.virtuslab.inkuire.model.*
import org.virtuslab.inkuire.plugin.content.InkuireContentPage
import org.virtuslab.inkuire.plugin.transformers.DefaultDokkaToSerializableModelTransformer
import org.virtuslab.inkuire.plugin.transformers.DefaultDokkaToSerializableModelTransformer.toSerializable
import org.virtuslab.inkuire.plugin.transformers.anyAndNothingAppender
import org.virtuslab.inkuire.plugin.transformers.functionalTypesNormalizerTransformer
import org.virtuslab.inkuire.plugin.transformers.javaPrimitivesMapper

object InkuireDocumentableToPageTranslator : DocumentableToPageTranslator {

    override fun invoke(module: DModule): ModulePageNode = module.packages.let { packages ->
        packages.flatMap {
            it.functions +
                    it.properties.mapNotNull { it.getter } +
                    it.classlikes.flatMap { classlike -> classlike.getFunctions() }
        }.javaPrimitivesMapper().functionalTypesNormalizerTransformer()
    }.let { functions ->
        ModulePageNode(
                name = "root",
                content = ContentText("", DCI(emptySet(), ContentKind.Empty), emptySet()),
                children = module.sourceSets.map { sourceSet ->
                    with(DefaultDokkaToSerializableModelTransformer) {
                        InkuireContentPage(
                                name = sourceSet.sourceSetID.sourceSetName,
                                functions = functions.filter { sourceSet in it.sourceSets }.map { it.toSerializable(sourceSet) }.distinct(), // That distinct removes copies of autogenerated/inherited functions from data classes/Any class
                                ancestryGraph = typesAncestryGraph(module, sourceSet).anyAndNothingAppender()
                        )
                    }
                },
                documentable = module
        )
    }

    private fun typesAncestryGraph(documentable: Documentable, sourceSet: DokkaConfiguration.DokkaSourceSet): List<AncestryGraph> {
        return documentable.children.filter { sourceSet in it.sourceSets }.fold(emptyList<AncestryGraph>()) { acc, elem ->
            acc + typesAncestryGraph(elem, sourceSet)
        } + documentable.toAncestryEntry(sourceSet)
    }


    private fun Documentable.toAncestryEntry(sourceSet: DokkaConfiguration.DokkaSourceSet): List<AncestryGraph> = when (this) {
        is DClasslike -> if (this is WithSupertypes) {
            listOf(AncestryGraph(dri.toSerializable(), (STypeConstructor(dri.toSerializable(), getPossibleGenerics())), (supertypes[sourceSet]?.map { it.typeConstructor.toSerializable() }
                    ?: emptyList())))
        } else {
            listOf(AncestryGraph(dri.toSerializable(), (STypeConstructor(dri.toSerializable(), getPossibleGenerics())), emptyList()))
        }
        is DTypeParameter ->
            listOf(AncestryGraph(dri.toSerializable(), (STypeParameter(dri.toSerializable(), name)), bounds.map { it.toSerializable() }))
        is DTypeAlias ->
            listOf(AncestryGraph(dri.toSerializable(), (STypeConstructor(dri.toSerializable(), getPossibleGenerics())), listOfNotNull(underlyingType[sourceSet]?.toSerializable())))
        else -> emptyList()
    } + if (this is WithGenerics)
        generics.flatMap { it.toAncestryEntry(sourceSet) }
    else emptyList()

    private fun Documentable.getPossibleGenerics() = if (this is WithGenerics) {
        this.generics.map {
            it.toSerializable().variantTypeParameter
        }
    } else {
        emptyList()
    }

    private fun DClasslike.getFunctions(): List<DFunction> = functions +
            classlikes.flatMap { it.getFunctions() } +
            properties.mapNotNull { it.getter } + if (this is DClass) constructors else emptyList()
}
