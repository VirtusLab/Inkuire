package org.virtuslab.inkuire.plugin

import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaPlugin

class InkuireDbGeneratorDokkaPlugin : DokkaPlugin() {

    private val dokkaBase by lazy { plugin<DokkaBase>() }

    val documentableToPageTranslator by extending {
        CoreExtensions.documentableToPageTranslator with InkuireDocumentableToPageTranslator override dokkaBase.documentableToPageTranslator
    }

    val renderer by extending {
        CoreExtensions.renderer providing ::InkuireRenderer override dokkaBase.htmlRenderer
    }
}
