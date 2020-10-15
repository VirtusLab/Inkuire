package org.virtuslab.inkuire.plugin.dbgenerator

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.renderers.Renderer
import org.virtuslab.inkuire.plugin.renderers.InkuireRenderer

class InkuireJustDbRenderer(context: DokkaContext) : Renderer {
    private val outputWriter = context.plugin<DokkaBase>().querySingle { outputWriter }

    override fun render(root: RootPageNode) = InkuireRenderer(outputWriter).render(root)
}
