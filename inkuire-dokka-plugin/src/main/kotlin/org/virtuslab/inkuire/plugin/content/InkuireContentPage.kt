package org.virtuslab.inkuire.plugin.content

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.*
import org.virtuslab.inkuire.model.*

data class InkuireContentPage(
        override val name: String,
        val functions: List<SDFunction>,
        val ancestryGraph: List<AncestryGraph>,
        override val content: ContentNode = ContentText("", DCI(emptySet(), ContentKind.Empty), emptySet()),
        override val children: List<PageNode> = emptyList(),
        override val documentable: Documentable? = null,
        override val dri: Set<DRI> = emptySet(),
        override val embeddedResources: List<String> = emptyList()
) : ContentPage {
    override fun modified(name: String, content: ContentNode, dri: Set<DRI>, embeddedResources: List<String>, children: List<PageNode>) =
            copy(name = name, children = children, dri = dri, embeddedResources = embeddedResources, content = content)
    override fun modified(name: String, children: List<PageNode>) = copy(name = name, children = children)
}