package serialization

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.pages.RenderingStrategy
import org.junit.Assert
import org.junit.Test
import org.virtuslab.inkuire.plugin.content.InkuireContentPage
import org.virtuslab.inkuire.plugin.translators.InkuireDocumentableToPageTranslator
import testApi.testRunner.defaultSourceSet

class FunctionAncestryEntryAppenderTest {

    @Test
    fun `functional type in ancestry graph`() {

        val module = DModule(
            "module",
            listOf(
                DPackage(
                    DRI(),
                    listOf(
                        DFunction(
                            DRI(),
                            "",
                            false,
                            emptyList(),
                            emptyMap(),
                            null,
                            emptyMap(),
                            emptyMap(),
                            FunctionalTypeConstructor(
                                DRI("kotlin", "Function3"),
                                listOf(
                                    Invariance(GenericTypeConstructor(DRI(), emptyList(), "")),
                                    Invariance(GenericTypeConstructor(DRI(), emptyList(), "")),
                                    Invariance(GenericTypeConstructor(DRI(), emptyList(), "")),
                                    Invariance(GenericTypeConstructor(DRI(), emptyList(), ""))
                                )
                            ),
                            emptyList(),
                            null,
                            emptyMap(),
                            setOf(defaultSourceSet),
                            false
                        )
                    ),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyMap(),
                    null,
                    setOf(defaultSourceSet)
                )
            ),
            emptyMap(),
            null,
            setOf(defaultSourceSet)
        )

        val after = InkuireDocumentableToPageTranslator { callback, sourceSet ->
            RenderingStrategy.Write(callback(null, sourceSet))
        }.invoke(module)

        after.find { it.name.contains("adb") }.run {
            this as InkuireContentPage
            Assert.assertTrue((strategy as RenderingStrategy.Write).text.contains("Function3"))
        }
    }
}
