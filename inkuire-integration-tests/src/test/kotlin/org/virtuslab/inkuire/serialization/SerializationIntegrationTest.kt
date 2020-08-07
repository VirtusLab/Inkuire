package org.virtuslab.inkuire.serialization

import org.jetbrains.dokka.testApi.testRunner.AbstractCoreTest
import org.junit.Test
import org.virtuslab.inkuire.engine.model.ConcreteType
import org.virtuslab.inkuire.engine.model.ExternalSignature
import org.virtuslab.inkuire.engine.model.InkuireDb
import org.virtuslab.inkuire.engine.model.Type
import org.virtuslab.inkuire.plugin.InkuireDokkaPlugin
import scala.collection.Seq
import java.io.File
import java.nio.file.Paths
import scala.jdk.javaapi.CollectionConverters.asScala

class SerializationIntegrationTest : AbstractCoreTest() {

    private val testDataDir = getTestDataDir("projects/basic-multiplatform/src").toAbsolutePath()

    val configuration = dokkaConfiguration {

        sourceSets {
            val common = sourceSet {
                moduleName = "example"
                displayName = "common"
                name = "common"
                analysisPlatform = "common"
                sourceRoots = listOf("commonMain").map {
                    Paths.get("$testDataDir/$it/kotlin").toString()
                }
            }
            sourceSet {
                moduleName = "example"
                displayName = "js"
                name = "js"
                analysisPlatform = "js"
                sourceRoots = listOf("jsMain", "commonMain").map {
                    Paths.get("$testDataDir/$it/kotlin").toString()
                }
                dependentSourceSets = setOf(common.sourceSetID)
            }
            sourceSet {
                moduleName = "example"
                displayName = "jvm"
                name = "jvm"
                analysisPlatform = "jvm"
                sourceRoots = listOf("jvmMain", "commonMain").map {
                    Paths.get("$testDataDir/$it/kotlin").toString()
                }
                dependentSourceSets = setOf(common.sourceSetID)
            }

        }
    }

    @Test
    fun `serialize and deserialize`() {

        var outputDir: String = ""

        testFromData(configuration, pluginOverrides = listOf(InkuireDokkaPlugin())) {
            renderingStage = { _, context ->
                outputDir = context.configuration.outputDir
            }
        }

        val parent = File(outputDir)

        val expectedSources = listOf("common", "js", "jvm").let {
            it.map { "ancestryGraph$it.json" } + it.map { "functions$it.json" }
        }

        assert(parent.walkTopDown().map { it.name }.toList().containsAll(expectedSources))

        val (functions, ancestors) = parent.walkTopDown().filter {
            "jvm" in it.name || "common" in it.name
        }.partition {
            "functions" in it.name
        }

        val inkuireDb = InkuireDb.read(asScala(functions).toList(), asScala(ancestors).toList()).toOption().get()

        assert(inkuireDb.functions().size() > 0)
        assert(inkuireDb.functions().findSignature("jsSpecificFun").isEmpty)

        val sig1 = inkuireDb.functions().findSignature("ClassWithFunctions·() → String")
        assert(sig1.isDefined)
        sig1.get().signature().run {
            assert((receiver().get() as ConcreteType).name().contains("ClassWithFunctions"))
            assert((result() as ConcreteType).name().contains("String"))
            assert(arguments().size() == 0)
        }

        val sig2 = inkuireDb.functions().findSignature("(String) → String")
        assert(sig2.isDefined)
        sig2.get().signature().run {
            assert(receiver().isEmpty)
            assert((result() as ConcreteType).name().contains("String"))
            assert(arguments().size() == 1)
            assert((arguments().head() as ConcreteType).name().contains("String"))
        }

        val sig3 = inkuireDb.functions().findSignature("String·(String) → String")
        assert(sig3.isDefined)
        sig3.get().signature().run {
            assert((receiver().get() as ConcreteType).name().contains("String"))
            assert((result() as ConcreteType).name().contains("String"))
            assert(arguments().size() == 1)
            assert((arguments().head() as ConcreteType).name().contains("String"))
        }

        assert(inkuireDb.types().size() > 0)
        assert(inkuireDb.types().findType("InheritingClass").let {
            if(it.isDefined) {
                it.get()._2.size() == 1
            } else {
                false
            }
        })
    }

    private fun Seq<ExternalSignature>.findSignature(name: String) = find {
        it.name().contains(name)
    }

    private fun scala.collection.immutable.Map<Type, scala.collection.immutable.Set<Type>>.findType(name: String) = find {
        (it._1 as ConcreteType).name().contains(name)
    }
}