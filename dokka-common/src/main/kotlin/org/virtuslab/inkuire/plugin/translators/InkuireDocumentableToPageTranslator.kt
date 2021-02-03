package org.virtuslab.inkuire.plugin.translators

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.DokkaSourceSetImpl
import org.jetbrains.dokka.analysis.DescriptorDocumentableSource
import org.jetbrains.dokka.analysis.KotlinAnalysis
import org.jetbrains.dokka.analysis.PsiDocumentableSource
import org.jetbrains.dokka.base.translators.psi.DefaultPsiToDocumentableTranslator
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.utilities.DokkaConsoleLogger
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor.Kind.FAKE_OVERRIDE
import org.virtuslab.inkuire.kotlin.model.*
import org.virtuslab.inkuire.kotlin.model.util.CustomGson
import org.virtuslab.inkuire.plugin.content.InkuireContentPage
import org.virtuslab.inkuire.plugin.transformers.DefaultDokkaToSerializableModelTransformer.toSerializable
import org.virtuslab.inkuire.plugin.transformers.anyAndNothingAppender
import org.virtuslab.inkuire.plugin.transformers.functionalTypesNormalizerTransformer
import org.virtuslab.inkuire.plugin.transformers.javaPrimitivesMapper
import org.virtuslab.inkuire.plugin.zip.Unzipper
import com.intellij.lang.jvm.JvmModifier
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute
import com.intellij.lang.jvm.types.JvmReferenceType
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.impl.source.PsiImmediateClassType
import com.intellij.psi.javadoc.PsiDocComment
import org.jetbrains.kotlin.build.JvmSourceRoot
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.jvm.config.JavaSourceRoot

class InkuireDocumentableToPageTranslator(val context: DokkaContext, val renderingStrategy: (callback: (DriResolver?, DokkaConfiguration.DokkaSourceSet) -> String, sourceSet: DokkaConfiguration.DokkaSourceSet) -> RenderingStrategy) {

    fun invoke(module: DModule): List<PageNode> = module.packages.let { packages ->
        packages.flatMap {
            it.functions +
                it.properties.mapNotNull { it.getter } +
                it.classlikes.flatMap { classlike -> classlike.getFunctions() }
        }.javaPrimitivesMapper().functionalTypesNormalizerTransformer()
    }.let { functions ->
        module.sourceSets.flatMap { sourceSet ->
            val callback: (DriResolver?, DokkaConfiguration.DokkaSourceSet) -> String = { locationResolver, sourceSet ->
                functions.filter {
                    sourceSet in it.sourceSets && it.isNotOverrideOrInherited(sourceSet)
                }.map { it.toSerializable(sourceSet, locationResolver) }.distinct() // That distinct removes copies of autogenerated/inherited functions from data classes/Any class
                    .toFunctionsJson()
            }
            val unzippedJars = Unzipper.unzipFiles(sourceSet.classpath)
            val newSourceSet = (sourceSet as DokkaSourceSetImpl).copy(sourceRoots = unzippedJars.toSet())
            val kotlinAnalysis = KotlinAnalysis(listOf(newSourceSet), DokkaConsoleLogger)

            val (environment, _) = kotlinAnalysis[newSourceSet]

            val sourceRoots = environment.configuration.get(CLIConfigurationKeys.CONTENT_ROOTS)
                ?.filterIsInstance<JvmSourceRoot>()
                ?.map { it.file }
                ?: listOf()
            val localFileSystem = VirtualFileManager.getInstance().getFileSystem("file")

            val psiFiles = sourceRoots.map { sourceRoot ->
                sourceRoot.absoluteFile.walkTopDown().mapNotNull {
                    localFileSystem.findFileByPath(it.path)?.let { vFile ->
                        PsiManager.getInstance(environment.project).findFile(vFile) as? PsiJavaFile
                    }
                }.toList()
            }.flatten()
            println(psiFiles)
            listOf(
                InkuireContentPage(
                    name = "scripts/${pathOfFdb(module, sourceSet)}",
                    strategy = renderingStrategy(callback, sourceSet)
                ),
                InkuireContentPage(
                    name = "scripts/${pathOfAdb(module, sourceSet)}",
                    strategy = RenderingStrategy.Write(typesAncestryGraph(module, sourceSet).distinct().anyAndNothingAppender().toAncestryGraphJson())
                )
            )
        }
    }.let {
        it + listOf(
            InkuireContentPage(
                name = "scripts/inkuire-config.json",
                strategy = RenderingStrategy.Write(
                    """
                        {
                          "address": {"address": "0.0.0.0"},
                          "port": { "port": 8080 },
                          "dbPaths": [
                            ${module.sourceSets.joinToString(separator = ", ") { "{\"path\" : \"${pathOfFdb(module, it)}\"}" }}
                          ],
                          "ancestryGraphPaths": [
                            ${module.sourceSets.joinToString(separator = ", ") { "{\"path\" : \"${pathOfAdb(module, it)}\"}" }}
                          ]
                        }
                    """.trimIndent()
                )
            )
        )
    }

    internal fun typesAncestryGraph(documentable: Documentable, sourceSet: DokkaConfiguration.DokkaSourceSet): List<AncestryGraph> {
        return documentable.children.filter { sourceSet in it.sourceSets }.fold(emptyList<AncestryGraph>()) { acc, elem ->
            acc + typesAncestryGraph(elem, sourceSet)
        } + documentable.toAncestryEntry(sourceSet, documentable)
    }

    private fun Documentable.toAncestryEntry(sourceSet: DokkaConfiguration.DokkaSourceSet, parent: Documentable): List<AncestryGraph> = when (this) {
        is DClasslike ->
            if (this is WithSupertypes) {
                listOf(
                    AncestryGraph(
                        dri.toSerializable(), (STypeConstructor(dri.toSerializable(), getPossibleGenerics())),
                        (
                            supertypes[sourceSet]?.map { it.typeConstructor.toSerializable() }
                                ?: emptyList()
                            )
                    )
                ) + (supertypes[sourceSet]?.flatMap { it.typeConstructor.possibleFunctionAncestryEntry() } ?: emptyList())
            } else {
                listOf(AncestryGraph(dri.toSerializable(), (STypeConstructor(dri.toSerializable(), getPossibleGenerics())), emptyList()))
            }
        is DTypeAlias ->
            listOf(AncestryGraph(dri.toSerializable(), (STypeConstructor(dri.toSerializable(), getPossibleGenerics())), listOfNotNull(underlyingType[sourceSet]?.toSerializable()))) +
                (underlyingType[sourceSet]?.possibleFunctionAncestryEntry() ?: emptyList())
        is DProperty -> listOfNotNull(
            receiver?.type,
            type,
            *(generics.flatMap { it.bounds }.toTypedArray())
        ).flatMap { it.possibleFunctionAncestryEntry() } + (getter?.toAncestryEntry(sourceSet, this) ?: emptyList())
        is DFunction -> listOfNotNull(
            receiver?.type,
            type,
            *(parameters.map { it.type }.toTypedArray()),
            *(generics.flatMap { it.bounds }.toTypedArray())
        ).flatMap { it.possibleFunctionAncestryEntry() }
        else -> emptyList()
    } + if (this is WithGenerics)
        generics.flatMap { it.toAncestryEntry(sourceSet, this) }
    else emptyList()

    private fun Documentable.getPossibleGenerics() = if (this is WithGenerics) {
        this.generics.map {
            it.toSerializable().variantTypeParameter
        }
    } else {
        emptyList()
    }

    private fun DClasslike.getFunctions(): List<DFunction> = (
        functions +
            classlikes.flatMap { it.getFunctions() } +
            properties.mapNotNull { it.getter } + if (this is DClass) constructors else emptyList()
        ).map {
        it.copy(
            generics = if (this is WithGenerics) (it.generics + this.generics).distinctBy { it.name } else it.generics
        )
    }

    private fun DFunction.isNotOverrideOrInherited(sourceSet: DokkaConfiguration.DokkaSourceSet): Boolean {
        return when (val src = this.sources[sourceSet]) {
            is DescriptorDocumentableSource -> {
                val desc = src.descriptor as CallableMemberDescriptor
                (!(desc.kind == FAKE_OVERRIDE || desc.overriddenDescriptors.isNotEmpty())) // parenthesis becuase Kotlin has stupid parser
            }
            is PsiDocumentableSource -> true // TODO: Find way of finding whether PsiMethod is actually override
            else -> true
        }
    }

    private fun Projection.possibleFunctionAncestryEntry(): List<AncestryGraph> = when (this) {
        is TypeParameter -> emptyList()
        is GenericTypeConstructor -> emptyList()
        is FunctionalTypeConstructor -> listOf(toFunctionAncestryEntry()) + projections.flatMap { it.possibleFunctionAncestryEntry() }
        is Nullable -> this.inner.possibleFunctionAncestryEntry()
        is TypeAliased -> this.inner.possibleFunctionAncestryEntry()
        is PrimitiveJavaType -> emptyList()
        Void -> emptyList()
        JavaObject -> emptyList()
        Dynamic -> emptyList()
        is UnresolvedBound -> emptyList()
        Star -> emptyList()
        is Variance<*> -> this.inner.possibleFunctionAncestryEntry()
    }

    private fun FunctionalTypeConstructor.toFunctionAncestryEntry(): AncestryGraph = AncestryGraph(
        dri.toSerializable(),
        STypeConstructor(
            dri.toSerializable(),
            (0..projections.size - 2).map {
                SContravariance(STypeParameter(SDRI("kotlin", "Function${projections.size - 1}", original = "kotlin/Function${projections.size - 1}///PointingToTypeParameter($it)/"), "P$it"))
            } + SCovariance(STypeParameter(SDRI("kotlin", "Function${projections.size - 1}", original = "kotlin/Function${projections.size - 1}///PointingToTypeParameter(${projections.size - 1})/"), "R")),
            SFunctionModifiers.FUNCTION
        ),
        listOf(
            STypeConstructor(
                SDRI("kotlin", "Function", original = "kotlin/Function///PointingToDeclaration/"),
                listOf(STypeParameter(SDRI("kotlin", "Function", original = "kotlin/Function///PointingToTypeParameter(0)/"), "R")),
                SFunctionModifiers.FUNCTION
            )
        )
    )

    private fun List<SDFunction>.toFunctionsJson(): String = CustomGson.instance.toJson(this)
    private fun List<AncestryGraph>.toAncestryGraphJson(): String = CustomGson.instance.toJson(this)

    private fun pathOfFdb(module: DModule, sourceSet: DokkaConfiguration.DokkaSourceSet): String =
        "inkuiredb/${sourceSet.sourceSetID.sourceSetName}/${module.name}.inkuire.fdb"
    private fun pathOfAdb(module: DModule, sourceSet: DokkaConfiguration.DokkaSourceSet): String =
        "inkuiredb/${sourceSet.sourceSetID.sourceSetName}/${module.name}.inkuire.adb"
}
