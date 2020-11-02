package serialization

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.virtuslab.inkuire.plugin.transformers.javaPrimitivesMapper
import utils.simpleFunction
import utils.simpleParameter

class PrimitivesMappingTest {

    @Test
    fun `map primitive types`() {

        val function = simpleFunction(
            DRI("some", "function"),
            "function",
            listOf(
                simpleParameter(
                    DRI("some", "int"),
                    "int",
                    PrimitiveJavaType("int")
                ),
                simpleParameter(
                    DRI("some", "char"),
                    "char",
                    PrimitiveJavaType("char")
                ),
                simpleParameter(
                    DRI("some", "object"),
                    "object",
                    JavaObject
                ),
            ),
            Void,
            null,
            emptyList()
        )

        val after = listOf(function).javaPrimitivesMapper()

        val expected = listOf(
            simpleFunction(
                DRI("some", "function"),
                "function",
                listOf(
                    simpleParameter(
                        DRI("some", "int"),
                        "int",
                        GenericTypeConstructor(DRI("kotlin", "Int"), emptyList())
                    ),
                    simpleParameter(
                        DRI("some", "char"),
                        "char",
                        GenericTypeConstructor(DRI("kotlin", "Char"), emptyList())
                    ),
                    simpleParameter(
                        DRI("some", "object"),
                        "object",
                        GenericTypeConstructor(DRI("kotlin", "Any"), emptyList())
                    ),
                ),
                GenericTypeConstructor(DRI("kotlin", "Unit"), emptyList()),
                null,
                emptyList()
            )
        )

        assertEquals(after, expected)
    }
}
