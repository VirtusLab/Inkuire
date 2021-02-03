package org.virtuslab.inkuire.plugin.dbgenerator

import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaPlugin

class InkuireDbGeneratorDokkaPlugin : DokkaPlugin() {

    private val dokkaBase by lazy { plugin<DokkaBase>() }

    val documentableToPageTranslator by extending {
        CoreExtensions.documentableToPageTranslator providing ::InkuireDbGeneratorDocumentableToPageTranslator override dokkaBase.documentableToPageTranslator
    }

    val renderer by extending {
        CoreExtensions.renderer providing ::InkuireJustDbRenderer override dokkaBase.htmlRenderer
    }
}
