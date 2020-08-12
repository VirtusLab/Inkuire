package org.virtuslab.inkuire.plugin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.renderers.Renderer
import org.virtuslab.inkuire.model.util.CustomGson
import org.virtuslab.inkuire.plugin.content.InkuireContentPage


class InkuireRenderer(context: DokkaContext) : Renderer {
    private val outputWriter = context.plugin<DokkaBase>().querySingle { outputWriter }

    override fun render(root: RootPageNode) = when(root) {
        is ModulePageNode -> runBlocking(Dispatchers.Default) {
            root.children.forEach {
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

    private fun PageNode.toFunctionsJson() = when(this) {
        is InkuireContentPage -> CustomGson.instance.toJson(functions)
        else -> throw UnsupportedOperationException("Content node is not Inkuiry content node")
    }

    private fun PageNode.toAncestryGraphJson() = when(this) {
        is InkuireContentPage -> CustomGson.instance.toJson(ancestryGraph)
        else -> throw UnsupportedOperationException("Content node is not Inkuiry content node")
    }

}