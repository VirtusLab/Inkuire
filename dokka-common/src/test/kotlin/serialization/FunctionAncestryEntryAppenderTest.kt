package serialization

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.pages.RenderingStrategy
import org.junit.Assert
import org.junit.Test
import org.virtuslab.inkuire.plugin.translators.InkuireDocumentableToPageTranslator
import testApi.testRunner.defaultSourceSet
import utils.simpleFunction
import utils.simpleParameter
import utils.simpleProperty
import utils.simpleTypeParameter

class FunctionAncestryEntryAppenderTest {

    val function = simpleFunction(
        DRI("some", "function"),
        "function",
        listOf(
            simpleParameter(
                DRI("some", "parameter1"),
                "parameter1",
                FunctionalTypeConstructor(
                    DRI("kotlin", "Function0"),
                    listOf(
                        GenericTypeConstructor(
                            DRI("kotlin", "Int"),
                            emptyList()
                        )
                    )
                )
            )
        ),
        FunctionalTypeConstructor(
            DRI("kotlin", "Function1"),
            listOf(
                GenericTypeConstructor(
                    DRI("kotlin", "Int"),
                    emptyList()
                ),
                FunctionalTypeConstructor(
                    DRI("kotlin", "Function2"),
                    listOf(
                        GenericTypeConstructor(
                            DRI("kotlin", "Int"),
                            emptyList()
                        ),
                        GenericTypeConstructor(
                            DRI("kotlin", "String"),
                            emptyList()
                        ),
                        GenericTypeConstructor(
                            DRI("kotlin", "String"),
                            emptyList()
                        )
                    )
                )
            )
        ),
        simpleParameter(
            DRI("some", "receiver"),
            "receiver",
            FunctionalTypeConstructor(
                DRI("kotlin", "Function3"),
                listOf(
                    GenericTypeConstructor(
                        DRI("kotlin", "Int"),
                        emptyList()
                    ),
                    GenericTypeConstructor(
                        DRI("kotlin", "String"),
                        emptyList()
                    ),
                    GenericTypeConstructor(
                        DRI("kotlin", "Int"),
                        emptyList()
                    ),
                    GenericTypeConstructor(
                        DRI("kotlin", "String"),
                        emptyList()
                    )
                )
            )
        ),
        listOf(
            simpleTypeParameter(
                Covariance(TypeParameter(DRI("some", "typeparam1"), "T")),
                listOf(
                    FunctionalTypeConstructor(
                        DRI("kotlin", "Function4"),
                        listOf(
                            GenericTypeConstructor(
                                DRI("kotlin", "Int"),
                                emptyList()
                            ),
                            GenericTypeConstructor(
                                DRI("kotlin", "String"),
                                emptyList()
                            ),
                            GenericTypeConstructor(
                                DRI("kotlin", "Int"),
                                emptyList()
                            ),
                            GenericTypeConstructor(
                                DRI("kotlin", "String"),
                                emptyList()
                            ),
                            GenericTypeConstructor(
                                DRI("kotlin", "Int"),
                                emptyList()
                            )
                        )
                    )
                )
            )
        )
    )

    val property = simpleProperty(
        DRI("some", "function"),
        "function",
        FunctionalTypeConstructor(
            DRI("kotlin", "Function1"),
            listOf(
                GenericTypeConstructor(
                    DRI("kotlin", "Int"),
                    emptyList()
                ),
                FunctionalTypeConstructor(
                    DRI("kotlin", "Function2"),
                    listOf(
                        GenericTypeConstructor(
                            DRI("kotlin", "Int"),
                            emptyList()
                        ),
                        GenericTypeConstructor(
                            DRI("kotlin", "String"),
                            emptyList()
                        ),
                        GenericTypeConstructor(
                            DRI("kotlin", "String"),
                            emptyList()
                        )
                    )
                )
            )
        ),
        null,
        emptyList(),
        function
    )

    @Test
    fun `salvage functional types from function`() {

        val afterTranlator = InkuireDocumentableToPageTranslator { _, _ -> RenderingStrategy.DoNothing }.typesAncestryGraph(
            function, defaultSourceSet
        )

        listOf("parameter" to 1, "result type" to 1, "result type inner type" to 1, "receiver" to 1, "type parameter" to 1).forEachIndexed { id, (elem, arity) ->
            Assert.assertEquals(
                "Could not extract Functional type for $elem",
                arity,
                afterTranlator.count { it.dri.original.contains("kotlin/Function$id") }
            )
        }
    }

    @Test
    fun `salvage functional types from property`() {

        val afterTranlator = InkuireDocumentableToPageTranslator { _, _ -> RenderingStrategy.DoNothing }.typesAncestryGraph(
            property, defaultSourceSet
        )

        listOf("parameter" to 1, "result type" to 2, "result type inner type" to 2, "receiver" to 1, "type parameter" to 1).forEachIndexed { id, (elem, arity) ->
            Assert.assertEquals(
                "Could not extract Functional type for $elem",
                arity,
                afterTranlator.count { it.dri.original.contains("kotlin/Function$id") }
            )
        }
    }
}
