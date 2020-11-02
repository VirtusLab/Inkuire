package org.virtuslab.inkuire.plugin.html

import org.jetbrains.dokka.base.translators.documentables.DefaultDocumentableToPageTranslator
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.transformers.documentation.DocumentableToPageTranslator
import org.virtuslab.inkuire.plugin.translators.InkuireDocumentableToPageTranslator

class InkuireHtmlExtensionDocumentableToPageTranslator(private val defaultDocumentableToPageTranslator: DefaultDocumentableToPageTranslator) : DocumentableToPageTranslator {
    override fun invoke(module: DModule): RootPageNode {
        val modulePageNode = defaultDocumentableToPageTranslator.invoke(module)
        return modulePageNode.modified(
            children = modulePageNode.children + InkuireDocumentableToPageTranslator.invoke(module)
        )
    }
}
