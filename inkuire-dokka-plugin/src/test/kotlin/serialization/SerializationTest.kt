package serialization

import org.jetbrains.dokka.testApi.testRunner.AbstractCoreTest
import org.junit.Ignore
import org.junit.Test
import org.virtuslab.inkuire.plugin.InkuireDokkaPlugin
import org.virtuslab.inkuire.plugin.content.InkuireContentPage
import utils.TestOutputWriterPlugin

class SerializationTest : AbstractCoreTest() {


    private val testDataDir = getTestDataDir("sources/src").toAbsolutePath()

    private val configuration = dokkaConfiguration {
        sourceSets {
            sourceSet {
                moduleName = "example"
                displayName = "jvm"
                name = "jvm"
                analysisPlatform = "jvm"
                sourceRoots = listOf("$testDataDir/main/kotlin")
            }
        }
    }

    private val writerPlugin = TestOutputWriterPlugin()
    private val inkuirePlugin = InkuireDokkaPlugin()

    @Ignore
    @Test
    fun `only one sourceSet`() {
        testFromData(
            configuration,
            pluginOverrides = listOf(inkuirePlugin, writerPlugin)
        ) {
            renderingStage = { root, context ->
                val jvmSourceSet = root.children.single() as InkuireContentPage
                assert(jvmSourceSet.name == "jvm") // There is correctly recognized sourceSet
                assert(jvmSourceSet.functions.size == jvmSourceSet.functions.distinct().size) // There are no redundant functions

                val functions = writerPlugin.writer.contents["functionsjvm.json"]
                val ancestors = writerPlugin.writer.contents["ancestryGraphjvm.json"]

                assert(functions != null)
                assert(ancestors != null)
            }
        }
    }
}