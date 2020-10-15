package org.virtuslab.inkuire.plugin.renderers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.dokka.base.renderers.OutputWriter
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.plugin
import org.virtuslab.inkuire.model.util.CustomGson
import org.virtuslab.inkuire.plugin.content.InkuireContentPage

class InkuireRenderer(val outputWriter: OutputWriter) {

    fun render(root: RootPageNode) = when (root) {
        is ModulePageNode -> runBlocking(Dispatchers.Default) {
            root.children.filterIsInstance<InkuireContentPage>().forEach {
                launch {
                    outputWriter.write(
                        "functions${it.name}",
                        it.toFunctionsJson(),
                        ".json"
                    )
                }
                launch {
                    outputWriter.write(
                        "ancestryGraph${it.name}",
                        it.toAncestryGraphJson(),
                        ".json"
                    )
                }
            }
        }
        else -> throw UnsupportedOperationException("Root page node is not module page node")
    }

    private fun InkuireContentPage.toFunctionsJson() = CustomGson.instance.toJson(functions)
    private fun InkuireContentPage.toAncestryGraphJson() = CustomGson.instance.toJson(ancestryGraph)
}
