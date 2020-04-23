package org.virtuslab.inkuire.plugin.content

import org.jetbrains.dokka.model.properties.PropertyContainer
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.DCI
import org.jetbrains.dokka.pages.PlatformData
import org.jetbrains.dokka.pages.Style
import org.virtuslab.inkuire.plugin.model.SDModule

data class InkuireContentNode(
        val inkuireModelRoot: SDModule,
        override val dci: DCI,
        override val platforms: Set<PlatformData>,
        override val style: Set<Style> = emptySet(),
        override val extra: PropertyContainer<ContentNode> = PropertyContainer.empty()
) : ContentNode {
    override fun withNewExtras(newExtras: PropertyContainer<ContentNode>): ContentNode = copy(extra = newExtras)
}