package org.virtuslab.inkuire.serialization

import org.jetbrains.dokka.testApi.testRunner.AbstractCoreTest
import org.jetbrains.kotlin.idea.debugger.readAction
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.virtuslab.inkuire.engine.model.ConcreteType
import org.virtuslab.inkuire.engine.model.ExternalSignature
import org.virtuslab.inkuire.engine.model.InkuireDb
import org.virtuslab.inkuire.engine.model.Type
import org.virtuslab.inkuire.plugin.InkuireDokkaPlugin
import scala.collection.Seq
import scala.jdk.javaapi.CollectionConverters.asJava
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

    lateinit var parent: File
    lateinit var inkuireDb: InkuireDb

    @Before
    fun setupTest() {

        lateinit var outputDir: String

        testFromData(configuration, pluginOverrides = listOf(InkuireDokkaPlugin())) {
            renderingStage = { _, context ->
                outputDir = context.configuration.outputDir
            }
        }

        parent = File(outputDir)

        val (functions, ancestors) = parent.walkTopDown().filter {
            "jvm" in it.name
        }.partition {
            "functions" in it.name
        }

        inkuireDb = InkuireDb.read(asScala(functions).toList(), asScala(ancestors).toList()).toOption().get()
    }


    @Test
    fun `serialize and deserialize`() {
        val expectedSources = listOf("common", "js", "jvm").let {
            it.map { "ancestryGraph$it.json" } + it.map { "functions$it.json" }
        }
        assert(parent.walkTopDown().map { it.name }.toList().containsAll(expectedSources))
        assert(inkuireDb.functions().size() > 0)
        assert(inkuireDb.functions().findSignature("jsSpecificFun").isEmpty())

        assert(inkuireDb.functions().size() > 0)
        assert(inkuireDb.functions().findSignature("jsSpecificFun").isEmpty())
    }

    @Test
    fun `deserialize ClassWithFunctions·() → String`() {
        val sig = inkuireDb.functions().findSignature("ClassWithFunctions·() → String").single()
        sig.signature().run {
            assert((receiver().get() as ConcreteType).name().contains("ClassWithFunctions"))
            assert((result() as ConcreteType).name().contains("String"))
            assert(arguments().size() == 0)
        }
    }

    @Test
    fun `deserialize (String) → String`() {
        val sig = inkuireDb.functions().findSignature("(String) → String").single()
        sig.signature().run {
            assert(receiver().isEmpty)
            assert((result() as ConcreteType).name().contains("String"))
            assert(arguments().size() == 1)
            assert((arguments().head() as ConcreteType).name().contains("String"))
        }
    }

    @Test
    fun `deserialize String·(String) → String`() {
        val sig = inkuireDb.functions().findSignature("String·(String) → String").single()
        sig.signature().run {
            assert((receiver().get() as ConcreteType).name().contains("String"))
            assert((result() as ConcreteType).name().contains("String"))
            assert(arguments().size() == 1)
            assert((arguments().head() as ConcreteType).name().contains("String"))
        }
    }

    @Test
    fun `deserialize InheritingClass`() {
        assert(inkuireDb.types().size() > 0)
        assert(inkuireDb.types().findType("InheritingClass").let {
            if (it.isDefined) {
                it.get()._2.size() == 1
            } else {
                false
            }
        })
    }

    @Test
    fun `deserialize String·(String, Int = 1, Boolean = true) → Float`() {
        val sig = inkuireDb.functions().findSignature("String·(String, Int = 1, Boolean = true) → Float")
        assert(sig.size == 4)

        val fullSig = sig.singleOrNull { it.signature().arguments().size() == 3 }
        assert(fullSig != null)

        val halfSig = sig.filter { it.signature().arguments().size() == 2 }
        assert(halfSig.size == 2)

        val microSig = sig.singleOrNull { it.signature().arguments().size() == 1 }
        assert(microSig != null)
    }

    private fun Seq<ExternalSignature>.findSignature(name: String) = asJava(filter {
        it.name() == name
    })

    private fun scala.collection.immutable.Map<Type, scala.collection.immutable.Set<Type>>.findType(name: String) = find {
        (it._1 as ConcreteType).name().contains(name)
    }
}