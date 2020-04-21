package org.virtuslab.inkuire.plugin

import org.jetbrains.dokka.base.renderers.FileWriter
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.renderers.Renderer
import org.jetbrains.kotlin.utils.addToStdlib.cast

class InkuireRenderer(val context: DokkaContext) : Renderer {

    override fun render(root: RootPageNode) =
        FileWriter(context).write(
                context.configuration.outputDir,
                root.cast<ModulePageNode>().content.cast<ContentText>().text,
                ".json"
        )
}