package serialization

import org.jetbrains.dokka.links.SelfType
import org.jetbrains.dokka.links.TypeReference
import org.jetbrains.dokka.testApi.testRunner.AbstractCoreTest
import org.junit.Test
import org.virtuslab.inkuire.plugin.InkuireDokkaPlugin
import utils.TestOutputWriterPlugin

class SerializationTest : AbstractCoreTest() {
    private val configuration = dokkaConfiguration {
        passes {
            pass {
                sourceRoots = listOf("src/")
            }
        }
        format = "inkuire"
    }
    @Test
    fun singleFunctionTest() {
        val outputWriter = TestOutputWriterPlugin()
        testInline(
        """
            |/src/main/kotlin/basic/Test.kt
            |package test
            |
            |fun test(a: Int){
            |    return 2
            |}
        """.trimMargin(),
                configuration,
                pluginOverrides = listOf(InkuireDokkaPlugin(), outputWriter)
        ) {
            fun TypeReference() : TypeReference = SelfType
            renderingStage = { root, context -> //TODO: Find some nice assertions.
            }
        }
    }
}