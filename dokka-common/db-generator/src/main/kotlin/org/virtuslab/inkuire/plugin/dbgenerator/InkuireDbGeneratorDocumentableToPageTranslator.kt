package org.virtuslab.inkuire.plugin.dbgenerator

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.transformers.documentation.DocumentableToPageTranslator
import org.virtuslab.inkuire.plugin.translators.InkuireDocumentableToPageTranslator

object InkuireDbGeneratorDocumentableToPageTranslator : DocumentableToPageTranslator {

    override fun invoke(module: DModule): RootPageNode = ModulePageNode(
        name = "root",
        content = ContentText("", DCI(emptySet(), ContentKind.Empty), emptySet()),
        children = (
            object : InkuireDocumentableToPageTranslator() {
                override fun renderingStrategy(callback: (LocationResolver?, DokkaConfiguration.DokkaSourceSet) -> String, sourceSet: DokkaConfiguration.DokkaSourceSet): RenderingStrategy =
                    RenderingStrategy.Write(callback(null, sourceSet))
            }
            ).invoke(module),
        documentable = module
    )
}
