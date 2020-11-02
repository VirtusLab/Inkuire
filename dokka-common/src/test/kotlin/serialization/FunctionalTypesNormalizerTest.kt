package serialization

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.virtuslab.inkuire.plugin.transformers.functionalTypesNormalizerTransformer
import utils.simpleFunction
import utils.simpleParameter
import utils.simpleTypeParameter

class FunctionalTypesNormalizerTest {

    @Test
    fun `map functional type`() {

        val function = simpleFunction(
            DRI("some", "function"),
            "function",
            listOf(
                simpleParameter(
                    DRI("some", "parameter1"),
                    "parameter1",
                    FunctionalTypeConstructor(
                        DRI("kotlin", "Function1"),
                        listOf(
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
                )
            ),
            FunctionalTypeConstructor(
                DRI("kotlin.jvm.functions", "Function2"),
                listOf(
                    GenericTypeConstructor(
                        DRI("kotlin", "Int"),
                        emptyList()
                    ),
                    GenericTypeConstructor(
                        DRI("kotlin", "String"),
                        emptyList()
                    ),
                    FunctionalTypeConstructor(
                        DRI("kotlin.jvm.functions", "Function1"),
                        listOf(
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
                )
            ),
            simpleParameter(
                DRI("some", "receiver"),
                "receiver",
                FunctionalTypeConstructor(
                    DRI("kotlin.jvm.functions", "Function1"),
                    listOf(
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
                            DRI("kotlin.jvm.functions", "Function1"),
                            listOf(
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
                    )
                ),
                simpleTypeParameter(
                    Covariance(TypeParameter(DRI("some", "typeparam1"), "T")),
                    listOf(
                        FunctionalTypeConstructor(
                            DRI("kotlin", "Function1"),
                            listOf(
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
                    )
                ),
            )
        )

        val after = listOf(function).functionalTypesNormalizerTransformer()

        val expected = listOf(
            simpleFunction(
                DRI("some", "function"),
                "function",
                listOf(
                    simpleParameter(
                        DRI("some", "parameter1"),
                        "parameter1",
                        FunctionalTypeConstructor(
                            DRI("kotlin", "Function1"),
                            listOf(
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
                    )
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
                        FunctionalTypeConstructor(
                            DRI("kotlin", "Function1"),
                            listOf(
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
                    )
                ),
                simpleParameter(
                    DRI("some", "receiver"),
                    "receiver",
                    FunctionalTypeConstructor(
                        DRI("kotlin", "Function1"),
                        listOf(
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
                                DRI("kotlin", "Function1"),
                                listOf(
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
                        )
                    ),
                    simpleTypeParameter(
                        Covariance(TypeParameter(DRI("some", "typeparam1"), "T")),
                        listOf(
                            FunctionalTypeConstructor(
                                DRI("kotlin", "Function1"),
                                listOf(
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
                        )
                    ),
                )
            )
        )

        assertEquals(after, expected)
    }
}
