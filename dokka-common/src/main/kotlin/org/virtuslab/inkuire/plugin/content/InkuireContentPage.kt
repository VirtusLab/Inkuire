package org.virtuslab.inkuire.plugin.content

import org.jetbrains.dokka.pages.*

data class InkuireContentPage(
    override val name: String,
    override val strategy: RenderingStrategy,
    override val children: List<PageNode> = emptyList(),
) : RendererSpecificPage {
    override fun modified(name: String, children: List<PageNode>): PageNode {
        return InkuireContentPage(name = name, strategy = strategy, children = children)
    }
}
