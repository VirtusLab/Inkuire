package org.virtuslab.inkuire.plugin.html

import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.translators.documentables.DefaultDocumentableToPageTranslator
import org.jetbrains.dokka.plugability.DokkaPlugin

class InkuireHtmlExtensionDokkaPlugin : DokkaPlugin() {
    private val dokkaBase by lazy { plugin<DokkaBase>() }

    val documentableToPageTranslator by extending {
        CoreExtensions.documentableToPageTranslator providing {
            InkuireHtmlExtensionDocumentableToPageTranslator(
                DefaultDocumentableToPageTranslator(
                    it.single(dokkaBase.commentsToContentConverter),
                    it.single(dokkaBase.signatureProvider),
                    it.logger
                )
            )
        } override dokkaBase.documentableToPageTranslator
    }
}
