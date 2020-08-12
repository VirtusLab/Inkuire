package org.virtuslab.inkuire.serialization

import org.jetbrains.dokka.testApi.testRunner.AbstractCoreTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.virtuslab.inkuire.engine.model.ConcreteType
import org.virtuslab.inkuire.engine.model.ExternalSignature
import org.virtuslab.inkuire.engine.model.InkuireDb
import org.virtuslab.inkuire.engine.model.Type
import org.virtuslab.inkuire.engine.model.*
import org.virtuslab.inkuire.plugin.InkuireDokkaPlugin
import scala.Tuple2
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
                sourceRoots = listOf("jsMain").map {
                    Paths.get("$testDataDir/$it/kotlin").toString()
                }
                dependentSourceSets = setOf(common.sourceSetID)
            }
            sourceSet {
                moduleName = "example"
                displayName = "jvm"
                name = "jvm"
                analysisPlatform = "jvm"
                sourceRoots = listOf("jvmMain").map {
                    Paths.get("$testDataDir/$it/kotlin").toString()
                } + listOf("jvmMain").map {
                    Paths.get("$testDataDir/$it/java").toString()
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
            "jvm" in it.name || "common" in it.name
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
        assertTrue(parent.walkTopDown().map { it.name }.toList().containsAll(expectedSources))
        assertTrue(inkuireDb.functions().size() > 0)
        assertTrue(inkuireDb.functions().findSignature("jsSpecificFun").isEmpty())

        assertTrue(inkuireDb.functions().size() > 0)
        assertTrue(inkuireDb.functions().findSignature("jsSpecificFun").isEmpty())

        assertTrue(inkuireDb.types().size() > 0)
    }

    @Test
    fun `deserialize ClassWithFunctions·() → String`() {
        val sig = inkuireDb.functions().findSignature("ClassWithFunctions·() → String").single()
        sig.signature().run {
            assertTrue((receiver().get() as ConcreteType).name().contains("ClassWithFunctions"))
            assertTrue((result() as ConcreteType).name().contains("String"))
            assertTrue(arguments().size() == 0)
        }
    }

    @Test
    fun `deserialize (String) → String`() {
        val sig = inkuireDb.functions().findSignature("(String) → String").single()
        sig.signature().run {
            assertTrue(receiver().isEmpty)
            assertTrue((result() as ConcreteType).name().contains("String"))
            assertTrue(arguments().size() == 1)
            assertTrue((arguments().head() as ConcreteType).name().contains("String"))
        }
    }

    @Test
    fun `deserialize String·(String) → String`() {
        val sig = inkuireDb.functions().findSignature("String·(String) → String").single()
        sig.signature().run {
            assertTrue((receiver().get() as ConcreteType).name().contains("String"))
            assertTrue((result() as ConcreteType).name().contains("String"))
            assertTrue(arguments().size() == 1)
            assertTrue((arguments().head() as ConcreteType).name().contains("String"))
        }
    }

    @Test
    fun `deserialize String·(String, Int = 1, Boolean = true) → Float`() {
        val sig = inkuireDb.functions().findSignature("String·(String, Int = 1, Boolean = true) → Float")
        assertTrue(sig.size == 4)

        val fullSig = sig.singleOrNull { it.signature().arguments().size() == 3 }
        assertTrue(fullSig != null)

        val halfSig = sig.filter { it.signature().arguments().size() == 2 }
        assertTrue(halfSig.size == 2)

        val microSig = sig.singleOrNull { it.signature().arguments().size() == 1 }
        assertTrue(microSig != null)
    }

    @Test
    fun `deserialize ((String) → Int) → Unit`() {
        val sig = inkuireDb.functions().findSignature("((String) → Int) → Unit").single()

        val res = sig.signature().result()
        assertTrue(res.name() == "Unit")

        val args = sig.signature().arguments()
        assertTrue(args.size() == 1)
        assertTrue(args.head().name() == "Function1")

        assertTrue((args.head() as GenericType).params().size() == 2)
        assertTrue((args.head() as GenericType).params().apply(0).name() == "String")
        assertTrue((args.head() as GenericType).params().apply(1).name() == "Int")
    }

    @Test
    fun `deserialize (String·(String) → Int) → Unit`() {
        val sig = inkuireDb.functions().findSignature("(String·(String) → Int) → Unit").single()

        val res = sig.signature().result()
        assertTrue(res.name() == "Unit")

        val args = sig.signature().arguments()
        assertTrue(args.size() == 1)
        assertTrue(args.head().name() == "Function2")

        assertTrue((args.head() as GenericType).params().size() == 3)
        assertTrue((args.head() as GenericType).params().apply(0).name() == "String")
        assertTrue((args.head() as GenericType).params().apply(1).name() == "String")
        assertTrue((args.head() as GenericType).params().apply(2).name() == "Int")
    }

    @Test
    fun `deserialize InheritingClass`() {
        assertTrue(inkuireDb.types().findType("InheritingClass")._2._2.size() == 1)
    }

    @Test
    fun `deserialize InheritingClassFromGenericType`() {
        val input = inkuireDb.types().findType("tests/InheritingClassFromGenericType///PointingToDeclaration/")

        assertTrue(input._2._2.size() == 2)
        assertTrue(input._1 == input._2._1.dri().get())
        assertTrue(inkuireDb.types().get(input._2._1.params().apply(0).dri().get()).isDefined)
        assertTrue(inkuireDb.types().get(input._2._1.params().apply(1).dri().get()).isDefined)

        assertTrue(input._2._2.apply(0).name() == "Comparable")
        assertTrue(input._2._2.apply(1).name() == "Collection")
    }

    @Test
    fun `deserialize TypeAlias`() {
        val input = inkuireDb.types().findType("tests/TypeAlias///PointingToDeclaration/")

        assertTrue(input._2._2.size() == 1)
        assertTrue(input._1 == input._2._1.dri().get())
        assertTrue(inkuireDb.types().get(input._2._1.params().apply(0).dri().get()).isDefined)

        assertTrue(input._2._2.apply(0).name() == "Comparable")
    }

    private fun Seq<ExternalSignature>.findSignature(name: String) = asJava(filter {
        it.name() == name
    })

    private fun scala.collection.immutable.Map<DRI, Tuple2<Type, scala.collection.immutable.Seq<Type>>>.findType(name: String) = this.find {
        it._1.original() == name
    }.get()
}


