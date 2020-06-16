package org.virtuslab.inkuire.plugin

import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.plugability.DokkaPlugin

class InkuireDokkaPlugin : DokkaPlugin() {

    val documentableToPageTranslator by extending {
        CoreExtensions.documentableToPageTranslator with InkuireDocumentableToPageTranslator applyIf { format == "inkuire" }
    }

    val renderer by extending {
        CoreExtensions.renderer providing ::InkuireRenderer applyIf { format == "inkuire" }
    }
}
