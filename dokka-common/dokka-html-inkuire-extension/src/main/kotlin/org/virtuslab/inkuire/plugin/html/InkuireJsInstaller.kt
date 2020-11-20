package org.virtuslab.inkuire.plugin.html

import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.childrenOfType
import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.transformers.pages.PageTransformer
import org.virtuslab.inkuire.plugin.translators.InkuireDocumentableToPageTranslator

object InkuireJsInstaller : PageTransformer {

    override fun invoke(input: RootPageNode): RootPageNode {
        val resources = listOf("scripts/inkuire-worker.js", "scripts/inkuire.js", "styles/inkuire-styles.css", "images/inkuire-search.png")
        val dbFiles = input.childrenOfType<ModulePageNode>().flatMap {
            InkuireDocumentableToPageTranslator { callback, sourceSet ->
                RenderingStrategy.LocationResolvableWrite { locationResolver ->
                    callback(locationResolver, sourceSet)
                }
            }.invoke(it.documentable as DModule)
        }
        return input.modified(
            children = input.children + resources.map {
                RendererSpecificResourcePage(it, emptyList(), RenderingStrategy.Copy("/inkuire/$it"))
            } + dbFiles
        ).transformContentPagesTree {
            it.modified(
                embeddedResources = it.embeddedResources + resources
            )
        }
    }
}
