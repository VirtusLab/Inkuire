package org.virtuslab.inkuire.plugin

<<<<<<< HEAD
import com.google.gson.Gson
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.pages.ContentNode
=======
import kotlinx.coroutines.runBlocking
import org.jetbrains.dokka.base.renderers.FileWriter
import org.jetbrains.dokka.pages.ContentText
>>>>>>> WIP
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.renderers.Renderer
import org.virtuslab.inkuire.plugin.content.InkuireContentNode

class InkuireRenderer(val context: DokkaContext) : Renderer {
    protected val outputWriter = context.plugin<DokkaBase>().querySingle { outputWriter }
    override fun render(root: RootPageNode) = when(root){
        is ModulePageNode -> outputWriter.write(
                context.configuration.outputDir,
                convertToJson(root.content),
                ".json")
        else -> throw UnsupportedOperationException("Root page node is not module page node")
    }

    private fun convertToJson(content: ContentNode) = when(content) {
        is InkuireContentNode -> Gson().toJson(content.inkuireModelRoot)
        else -> throw UnsupportedOperationException("Content node is not Inkuiry content node")
    }
}