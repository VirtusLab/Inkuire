package org.virtuslab.inkuire.plugin.dbgenerator

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.pages.RenderingStrategy
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.renderers.Renderer
import org.virtuslab.inkuire.plugin.content.InkuireContentPage

class InkuireJustDbRenderer(context: DokkaContext) : Renderer {
    private val outputWriter = context.plugin<DokkaBase>().querySingle { outputWriter }

    override fun render(root: RootPageNode) = runBlocking {
        root.children.filterIsInstance<InkuireContentPage>().forEach {
            launch { outputWriter.write(it.name, (it.strategy as? RenderingStrategy.Write)?.text ?: "[]", "") }
        }
    }
}
