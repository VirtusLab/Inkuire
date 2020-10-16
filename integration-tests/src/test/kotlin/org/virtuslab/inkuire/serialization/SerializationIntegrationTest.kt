package org.virtuslab.inkuire.serialization

import org.jetbrains.dokka.testApi.testRunner.AbstractCoreTest
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.virtuslab.inkuire.engine.model.*
import org.virtuslab.inkuire.engine.model.ConcreteType
import org.virtuslab.inkuire.engine.model.ExternalSignature
import org.virtuslab.inkuire.engine.model.InkuireDb
import org.virtuslab.inkuire.engine.model.Type
import org.virtuslab.inkuire.plugin.dbgenerator.InkuireDbGeneratorDokkaPlugin
import scala.Option
import scala.Some
import scala.Tuple2
import scala.collection.Seq
import scala.jdk.javaapi.CollectionConverters.asJava
import scala.jdk.javaapi.CollectionConverters.asScala
import java.io.File
import java.nio.file.Paths

class SerializationIntegrationTest : AbstractCoreTest() {

    companion object : AbstractCoreTest() {

        private val testDataDir = getTestDataDir("projects/basic-multiplatform/src").toAbsolutePath()

        val configuration = dokkaConfiguration {
            moduleName = "example"
            sourceSets {
                val common = sourceSet {
                    displayName = "common"
                    name = "common"
                    analysisPlatform = "common"
                    sourceRoots = listOf("commonMain").map {
                        Paths.get("$testDataDir/$it/kotlin").toString()
                    }
                }
                sourceSet {
                    displayName = "js"
                    name = "js"
                    analysisPlatform = "js"
                    sourceRoots = listOf("jsMain").map {
                        Paths.get("$testDataDir/$it/kotlin").toString()
                    }
                    dependentSourceSets = setOf(common.value.sourceSetID)
                }
                sourceSet {
                    displayName = "jvm"
                    name = "jvm"
                    analysisPlatform = "jvm"
                    sourceRoots = listOf("jvmMain").map {
                        Paths.get("$testDataDir/$it/kotlin").toString()
                    } + listOf("jvmMain").map {
                        Paths.get("$testDataDir/$it/java").toString()
                    }
                    dependentSourceSets = setOf(common.value.sourceSetID)
                }
            }
        }

        lateinit var parent: File
        lateinit var inkuireDb: InkuireDb

        @BeforeClass
        @JvmStatic
        fun setupTest() {

            testFromData(configuration, pluginOverrides = listOf(InkuireDbGeneratorDokkaPlugin())) {
                renderingStage = { _, context ->
                    parent = context.configuration.outputDir
                }
            }

            val (functions, ancestors) = parent.walkTopDown().filter {
                "jvm" in it.name || "common" in it.name
            }.partition {
                "functions" in it.name
            }.let {
                Pair(
                    it.first.map { it.toURI().toURL() },
                    it.second.map { it.toURI().toURL() }
                )
            }

            inkuireDb = InkuireDb.read(asScala(functions).toList(), asScala(ancestors).toList()).toOption().get()
        }
    }

    @Test
    fun `serialize and deserialize`() {
        val expectedSources = listOf("common", "js", "jvm").let {
            it.map { "ancestryGraphexample_$it.inkuire.adb" } + it.map { "functionsexample_$it.inkuire.fdb" }
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
            assertTrue((receiver().get().typ() as ConcreteType).name().name().contains("ClassWithFunctions"))
            assertTrue((result().typ() as ConcreteType).name().name().contains("String"))
            assertEquals(arguments().size(), 0)
        }
    }

    @Test
    fun `deserialize (String) → String`() {
        val sig = inkuireDb.functions().findSignature("(String) → String").single()
        sig.signature().run {
            assertTrue(receiver().isEmpty)
            assertTrue((result().typ() as ConcreteType).name().name().contains("String"))
            assertEquals(arguments().size(), 1)
            assertTrue((arguments().head().typ() as ConcreteType).name().name().contains("String"))
        }
    }

    @Test
    fun `deserialize String·(String) → String`() {
        val sig = inkuireDb.functions().findSignature("String·(String) → String").single()
        sig.signature().run {
            assertTrue((receiver().get().typ() as ConcreteType).name().name().contains("String"))
            assertTrue((result().typ() as ConcreteType).name().name().contains("String"))
            assertEquals(arguments().size(), 1)
            assertTrue((arguments().head().typ() as ConcreteType).name().name().contains("String"))
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
        assertEquals(res.typ().name().name(), "Unit")

        val args = sig.signature().arguments()
        assertEquals(args.size(), 1)
        assertEquals(args.head().typ().name().name(), "Function1")

        assertEquals((args.head().typ() as GenericType).params().size(), 2)
        assertEquals((args.head().typ() as GenericType).params().apply(0).typ().name().name(), "String")
        assertEquals((args.head().typ() as GenericType).params().apply(1).typ().name().name(), "Int")
    }

    @Test
    fun `deserialize (String·(String) → Int) → Unit`() {
        val sig = inkuireDb.functions().findSignature("(String·(String) → Int) → Unit").single()

        val res = sig.signature().result()
        assertEquals(res.typ().name().name(), "Unit")

        val args = sig.signature().arguments()
        assertEquals(args.size(), 1)
        assertEquals(args.head().typ().name().name(), "Function2")

        assertEquals((args.head().typ() as GenericType).params().size(), 3)
        assertEquals((args.head().typ() as GenericType).params().apply(0).typ().name().name(), "String")
        assertEquals((args.head().typ() as GenericType).params().apply(1).typ().name().name(), "String")
        assertEquals((args.head().typ() as GenericType).params().apply(2).typ().name().name(), "Int")
    }

    @Test
    fun `deserialize InheritingClass`() {
        assertEquals(inkuireDb.types().findType("tests/InheritingClass///PointingToDeclaration/").single().value._2.size(), 1)
    }

    @Test
    fun `deserialize Collection covariance parameter`() {
        val input = inkuireDb.types().findType("tests/Collection///PointingToDeclaration/")
        assertEquals(input.single().value._1.params().apply(0)::class.java, Covariance::class.java)
    }

    @Test
    fun `deserialize A1 contravariance parameter`() {
        val input = inkuireDb.types().findType("tests/A1///PointingToDeclaration/")
        assertEquals(input.single().value._1.params().apply(0)::class.java, Contravariance::class.java)
    }

    @Test
    fun `deserialize A2 covariance parameter`() {
        val input = inkuireDb.types().findType("tests/Collection///PointingToDeclaration/")
        assertEquals(input.single().value._1.params().apply(0)::class.java, Covariance::class.java)
    }

    @Ignore // this tests doesn't make sense anymore, since we don't have type parameters in AncestryGraph
    @Test
    fun `deserialize weirdFlexButOk covariance parameter`() {
        val r = inkuireDb.types().findType("tests//weirdFlexButOk/kotlin.CharSequence#TypeParam(bounds=[tests.B2[TypeParam(bounds=[kotlin.Any])]])#kotlin.Function2[kotlin.Int,kotlin.Char,TypeParam(bounds=[kotlin.Any])?]/PointingToGenericParameters(0)/").single()
        val c = inkuireDb.types().findType("tests//weirdFlexButOk/kotlin.CharSequence#TypeParam(bounds=[tests.B2[TypeParam(bounds=[kotlin.Any])]])#kotlin.Function2[kotlin.Int,kotlin.Char,TypeParam(bounds=[kotlin.Any])?]/PointingToGenericParameters(1)/").single()

        assertEquals(r.value._1.name().name(), "R")
        assertEquals(r.value._2.apply(0).name().name(), "Any")

        assertEquals(c.value._1.name().name(), "C")
        assertEquals((c.value._2.apply(0) as GenericType).base().name().name(), "B2")
        assertEquals((c.value._2.apply(0) as GenericType).params().apply(0)::class.java, Contravariance::class.java)
        assertEquals(((c.value._2.apply(0) as GenericType).params().apply(0).typ() as TypeVariable).name().name(), "R")
        assertEquals(
            ((c.value._2.apply(0) as GenericType).params().apply(0).typ() as TypeVariable).dri().get().original(),
            "tests//weirdFlexButOk/kotlin.CharSequence#TypeParam(bounds=[tests.B2[TypeParam(bounds=[kotlin.Any])]])#kotlin.Function2[kotlin.Int,kotlin.Char,TypeParam(bounds=[kotlin.Any])?]/PointingToGenericParameters(0)/"
        )
    }

    @Test
    fun `deserialize InheritingClassFromGenericType`() {
        val input = inkuireDb.types().findType("tests/InheritingClassFromGenericType///PointingToDeclaration/")

        assertEquals(input.single().value._2.size(), 2)
        assertEquals(input.single().key, input.single().value._1.dri().get())

        assertEquals(input.single().value._2.apply(0).name().name(), "Comparable")
        assertEquals(input.single().value._2.apply(1).name().name(), "Collection")
//        until we decide whether we should propagate variance, it should be invariant as in signature
//        assertEquals(input.single().value._2.apply(1).params().apply(0)::class.java, Covariance::class.java)
    }

    @Test
    fun `deserialize TypeAlias`() {
        val input = inkuireDb.types().findType("tests/TypeAlias///PointingToDeclaration/")

        assertEquals(input.single().value._2.size(), 1)
        assertEquals(input.single().key, input.single().value._1.dri().get())

        assertEquals(input.single().value._2.apply(0).name().name(), "Comparable")
    }

    @Test
    fun `deserialize ClassWithFunctions·() → Unit`() {
        val sig = inkuireDb.functions().findSignature("ClassWithFunctions·() → Unit").single()

        val receiver = sig.signature().receiver()
        assertEquals(
            receiver.get().typ().dri().get(),
            DRI(
                Some("tests"),
                Some("ClassWithFunctions"),
                Option.apply(null),
                "tests/ClassWithFunctions///PointingToDeclaration/"
            )
        )
    }

    @Test
    fun `deserialize extension property String·() → Int`() {
        val sig = inkuireDb.functions().findSignature("getString·() → Int").single()
        sig.signature().run {
            assertTrue((receiver().get().typ() as ConcreteType).name().name().contains("String"))
            assertTrue((result().typ() as ConcreteType).name().name().contains("Int"))
            assertEquals(arguments().size(), 0)
        }
    }

    @Test
    fun `deserialize getter of property ClassWithFunctions returning Int`() {
        val sig = inkuireDb.functions().findSignature("getClassWithFunctions·() → Int").single()
        sig.signature().run {
            assertTrue((receiver().get().typ() as ConcreteType).name().name().contains("ClassWithFunctions"))
            assertTrue((result().typ() as ConcreteType).name().name().contains("Int"))
            assertEquals(arguments().size(), 0)
        }
    }

    @Test
    fun `deserialize getter of extension property ClassWithFunctions getting String returning Int`() {
        val sig = inkuireDb.functions().findSignature("getWith(ClassWithFunctions) { String·() → Int }").single()
        sig.signature().run {
            assertTrue((receiver().get().typ() as ConcreteType).name().name().contains("String"))
            assertTrue((result().typ() as ConcreteType).name().name().contains("Int"))
            assertEquals(arguments().size(), 0)
        }
    }

    private fun Seq<ExternalSignature>.findSignature(name: String) = asJava(
        filter {
            it.name() == name
        }
    )

    private fun scala.collection.immutable.Map<DRI, Tuple2<Type, scala.collection.immutable.Seq<Type>>>.findType(name: String) = asJava(this).filter { (key, _) ->
        key.original() == name
    }.entries.toList()
}
