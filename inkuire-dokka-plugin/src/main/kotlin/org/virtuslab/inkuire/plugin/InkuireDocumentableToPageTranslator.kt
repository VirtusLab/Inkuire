package org.virtuslab.inkuire.plugin

import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.DCI
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.transformers.documentation.DocumentableToPageTranslator
import org.virtuslab.inkuire.plugin.content.InkuireContentNode
import org.virtuslab.inkuire.plugin.transformers.DefaultDokkaToSerializableModelTransformer
import org.virtuslab.inkuire.plugin.transformers.DokkaToSerializableModelTransformer

object InkuireDocumentableToPageTranslator : DocumentableToPageTranslator {
    private val transformer : DokkaToSerializableModelTransformer = DefaultDokkaToSerializableModelTransformer()

    override fun invoke(module: DModule) = ModulePageNode(
            name = "root",
            content = InkuireContentNode(
                    inkuireModelRoot = transformer.transform(module),
                    dci = DCI(emptySet(), ContentKind.Main),
                    platforms = emptySet()
            ),
            children = emptyList(),
            documentable = module
    )
}
