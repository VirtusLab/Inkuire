package org.virtuslab.inkuire.plugin.dbgenerator

import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.transformers.documentation.DocumentableToPageTranslator
import org.virtuslab.inkuire.plugin.translators.InkuireDocumentableToPageTranslator

object InkuireDbGeneratorDocumentableToPageTranslator : DocumentableToPageTranslator {

    override fun invoke(module: DModule): RootPageNode = ModulePageNode(
        name = "root",
        content = ContentText("", DCI(emptySet(), ContentKind.Empty), emptySet()),
        children = InkuireDocumentableToPageTranslator.invoke(module),
        documentable = module
    )
}
