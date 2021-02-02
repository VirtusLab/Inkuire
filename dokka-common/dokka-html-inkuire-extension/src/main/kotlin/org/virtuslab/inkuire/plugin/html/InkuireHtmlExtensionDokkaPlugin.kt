package org.virtuslab.inkuire.plugin.html

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaPlugin

class InkuireHtmlExtensionDokkaPlugin : DokkaPlugin() {
    private val dokkaBase by lazy { plugin<DokkaBase>() }

    val inkuireJsInstaller by extending {
        dokkaBase.htmlPreprocessors with InkuireJsInstaller order {
            after(dokkaBase.rootCreator)
            before(dokkaBase.baseSearchbarDataInstaller)
        }
    }
}
