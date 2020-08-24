package org.virtuslab.inkuire.serialization

import org.jetbrains.dokka.testApi.testRunner.AbstractCoreTest
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import org.virtuslab.inkuire.engine.model.ConcreteType
import org.virtuslab.inkuire.engine.model.ExternalSignature
import org.virtuslab.inkuire.engine.model.InkuireDb
import org.virtuslab.inkuire.engine.model.Type
import org.virtuslab.inkuire.engine.model.*
import org.virtuslab.inkuire.plugin.InkuireDokkaPlugin
import scala.Option
import scala.Some
import scala.Tuple2
import scala.collection.Seq
import scala.jdk.javaapi.CollectionConverters.asJava
import java.io.File
import java.nio.file.Paths
import scala.jdk.javaapi.CollectionConverters.asScala

class SerializationIntegrationTest : AbstractCoreTest() {

    companion object : AbstractCoreTest() {

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

        @BeforeClass
        @JvmStatic
        fun setupTest() {

            testFromData(configuration, pluginOverrides = listOf(InkuireDokkaPlugin())) {
                renderingStage = { _, context ->
                    parent = context.configuration.outputDir
                }
            }

            val (functions, ancestors) = parent.walkTopDown().filter {
                "jvm" in it.name || "common" in it.name
            }.partition {
                "functions" in it.name
            }

            inkuireDb = InkuireDb.read(asScala(functions).toList(), asScala(ancestors).toList()).toOption().get()
        }
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
            assertTrue((receiver().get() as ConcreteType).name().name().contains("ClassWithFunctions"))
            assertTrue((result() as ConcreteType).name().name().contains("String"))
            assertEquals(arguments().size(), 0)
        }
    }

    @Test
    fun `deserialize (String) → String`() {
        val sig = inkuireDb.functions().findSignature("(String) → String").single()
        sig.signature().run {
            assertTrue(receiver().isEmpty)
            assertTrue((result() as ConcreteType).name().name().contains("String"))
            assertEquals(arguments().size(), 1)
            assertTrue((arguments().head() as ConcreteType).name().name().contains("String"))
        }
    }

    @Test
    fun `deserialize String·(String) → String`() {
        val sig = inkuireDb.functions().findSignature("String·(String) → String").single()
        sig.signature().run {
            assertTrue((receiver().get() as ConcreteType).name().name().contains("String"))
            assertTrue((result() as ConcreteType).name().name().contains("String"))
            assertEquals(arguments().size(), 1)
            assertTrue((arguments().head() as ConcreteType).name().name().contains("String"))
        }
    }

    @Test
    fun `deserialize String·(String, Int = 1, Boolean = true) → Float`() {
        val sig = inkuireDb.functions().findSignature("String·(String, Int = 1, Boolean = true) → Float")
        assertEquals(sig.size, 4)

        val fullSig = sig.singleOrNull { it.signature().arguments().size() == 3 }
        assertNotEquals(fullSig, null)

        val halfSig = sig.filter { it.signature().arguments().size() == 2 }
        assertEquals(halfSig.size, 2)

        val microSig = sig.singleOrNull { it.signature().arguments().size() == 1 }
        assertNotEquals(microSig, null)
    }

    @Test
    fun `deserialize ((String) → Int) → Unit`() {
        val sig = inkuireDb.functions().findSignature("((String) → Int) → Unit").single()

        val res = sig.signature().result()
        assertEquals(res.name().name(), "Unit")

        val args = sig.signature().arguments()
        assertEquals(args.size(), 1)
        assertEquals(args.head().name().name(), "Function1")

        assertEquals((args.head() as GenericType).params().size(), 2)
        assertEquals((args.head() as GenericType).params().apply(0).typ().name().name(), "String")
        assertEquals((args.head() as GenericType).params().apply(1).typ().name().name(), "Int")
    }

    @Test
    fun `deserialize (String·(String) → Int) → Unit`() {
        val sig = inkuireDb.functions().findSignature("(String·(String) → Int) → Unit").single()

        val res = sig.signature().result()
        assertEquals(res.name().name(), "Unit")

        val args = sig.signature().arguments()
        assertEquals(args.size(), 1)
        assertEquals(args.head().name().name(), "Function2")

        assertEquals((args.head() as GenericType).params().size(), 3)
        assertEquals((args.head() as GenericType).params().apply(0).typ().name().name(), "String")
        assertEquals((args.head() as GenericType).params().apply(1).typ().name().name(), "String")
        assertEquals((args.head() as GenericType).params().apply(2).typ().name().name(), "Int")
    }

    @Test
    fun `deserialize InheritingClass`() {
        assertEquals(inkuireDb.types().findType("tests/InheritingClass///PointingToDeclaration/")._2._2.size(), 1)
    }

    @Test
    fun `deserialize Collection covariance parameter`() {
        val input = inkuireDb.types().findType("tests/Collection///PointingToDeclaration/")
        assertEquals(input._2._1.params().apply(0)::class.java, Covariance::class.java)
    }

    @Test
    fun `deserialize InheritingClassFromGenericType`() {
        val input = inkuireDb.types().findType("tests/InheritingClassFromGenericType///PointingToDeclaration/")

        assertEquals(input._2._2.size(), 2)
        assertEquals(input._1, input._2._1.dri().get())
        assertTrue(inkuireDb.types().get(input._2._1.params().apply(0).typ().dri().get()).isDefined)
        assertTrue(inkuireDb.types().get(input._2._1.params().apply(1).typ().dri().get()).isDefined)

        assertEquals(input._2._2.apply(0).name().name(), "Comparable")
        assertEquals(input._2._2.apply(1).name().name(), "Collection")
        assertEquals(input._2._2.apply(1).params().apply(0)::class.java, Covariance::class.java) // TODO: make it pass
    }

    @Test
    fun `deserialize TypeAlias`() {
        val input = inkuireDb.types().findType("tests/TypeAlias///PointingToDeclaration/")

        assertEquals(input._2._2.size(), 1)
        assertEquals(input._1, input._2._1.dri().get())
        assertTrue(inkuireDb.types().get(input._2._1.params().apply(0).typ().dri().get()).isDefined)

        assertEquals(input._2._2.apply(0).name().name(), "Comparable")
    }

    @Test
    fun `deserialize ClassWithFunctions·() → Unit`() {
        val sig = inkuireDb.functions().findSignature("ClassWithFunctions·() → Unit").single()

        val receiver = sig.signature().receiver()
        assertEquals(receiver.get().dri().get(), DRI(
            Some("tests"),
            Some("ClassWithFunctions"),
            Option.apply(null),
            "tests/ClassWithFunctions//#/PointingToDeclaration/"
        ))
    }

    private fun Seq<ExternalSignature>.findSignature(name: String) = asJava(filter {
        it.name() == name
    })

    private fun scala.collection.immutable.Map<DRI, Tuple2<Type, scala.collection.immutable.Seq<Type>>>.findType(name: String) = this.find {
        it._1.original() == name
    }.get()
}


