package org.virtuslab.inkuire.plugin

import com.google.gson.Gson
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.pages.DCI
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.transformers.documentation.DocumentableToPageTranslator
import org.virtuslab.inkuire.plugin.model.toAdapter

object InkuireDocumentableToPageTranslator : DocumentableToPageTranslator {

    override fun invoke(module: DModule) = ModulePageNode(
            name = "root",
            content = ContentText(
                    text = Gson().toJson(module.toAdapter()).toString(),
                    dci = DCI(emptySet(), ContentKind.Main),
                    platforms = emptySet()
            ),
            children = emptyList(),
            documentable = module
    )
}