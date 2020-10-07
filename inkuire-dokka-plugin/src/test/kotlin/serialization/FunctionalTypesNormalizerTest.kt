package serialization

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Covariance
import org.jetbrains.dokka.model.FunctionModifiers
import org.jetbrains.dokka.model.TypeConstructor
import org.jetbrains.dokka.model.TypeParameter
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
                    TypeConstructor(
                        DRI("kotlin", "Function1"),
                        listOf(
                            TypeConstructor(
                                DRI("kotlin", "Int"),
                                emptyList(),
                                FunctionModifiers.NONE
                            ),
                            TypeConstructor(
                                DRI("kotlin", "String"),
                                emptyList(),
                                FunctionModifiers.NONE
                            )
                        ),
                        FunctionModifiers.FUNCTION
                    )
                )
            ),
            TypeConstructor(
                DRI("kotlin.jvm.functions", "Function2"),
                listOf(
                    TypeConstructor(
                        DRI("kotlin", "Int"),
                        emptyList(),
                        FunctionModifiers.NONE
                    ),
                    TypeConstructor(
                        DRI("kotlin", "String"),
                        emptyList(),
                        FunctionModifiers.NONE
                    ),
                    TypeConstructor(
                        DRI("kotlin.jvm.functions", "Function1"),
                        listOf(
                            TypeConstructor(
                                DRI("kotlin", "Int"),
                                emptyList(),
                                FunctionModifiers.NONE
                            ),
                            TypeConstructor(
                                DRI("kotlin", "String"),
                                emptyList(),
                                FunctionModifiers.NONE
                            )
                        ),
                        FunctionModifiers.FUNCTION
                    )
                ),
                FunctionModifiers.FUNCTION
            ),
            simpleParameter(
                DRI("some", "receiver"),
                "receiver",
                TypeConstructor(
                    DRI("kotlin.jvm.functions", "Function1"),
                    listOf(
                        TypeConstructor(
                            DRI("kotlin", "Int"),
                            emptyList(),
                            FunctionModifiers.NONE
                        ),
                        TypeConstructor(
                            DRI("kotlin", "String"),
                            emptyList(),
                            FunctionModifiers.NONE
                        )
                    ),
                    FunctionModifiers.FUNCTION
                )
            ),
            listOf(
                simpleTypeParameter(
                    Covariance(TypeParameter(DRI("some", "typeparam1"), "T")),
                    listOf(
                        TypeConstructor(
                            DRI("kotlin.jvm.functions", "Function1"),
                            listOf(
                                TypeConstructor(
                                    DRI("kotlin", "Int"),
                                    emptyList(),
                                    FunctionModifiers.NONE
                                ),
                                TypeConstructor(
                                    DRI("kotlin", "String"),
                                    emptyList(),
                                    FunctionModifiers.NONE
                                )
                            ),
                            FunctionModifiers.FUNCTION
                        )
                    )
                ),
                simpleTypeParameter(
                    Covariance(TypeParameter(DRI("some", "typeparam1"), "T")),
                    listOf(
                        TypeConstructor(
                            DRI("kotlin", "Function1"),
                            listOf(
                                TypeConstructor(
                                    DRI("kotlin", "Int"),
                                    emptyList(),
                                    FunctionModifiers.NONE
                                ),
                                TypeConstructor(
                                    DRI("kotlin", "String"),
                                    emptyList(),
                                    FunctionModifiers.NONE
                                )
                            ),
                            FunctionModifiers.FUNCTION
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
                        TypeConstructor(
                            DRI("kotlin", "Function1"),
                            listOf(
                                TypeConstructor(
                                    DRI("kotlin", "Int"),
                                    emptyList(),
                                    FunctionModifiers.NONE
                                ),
                                TypeConstructor(
                                    DRI("kotlin", "String"),
                                    emptyList(),
                                    FunctionModifiers.NONE
                                )
                            ),
                            FunctionModifiers.FUNCTION
                        )
                    )
                ),
                TypeConstructor(
                    DRI("kotlin", "Function2"),
                    listOf(
                        TypeConstructor(
                            DRI("kotlin", "Int"),
                            emptyList(),
                            FunctionModifiers.NONE
                        ),
                        TypeConstructor(
                            DRI("kotlin", "String"),
                            emptyList(),
                            FunctionModifiers.NONE
                        ),
                        TypeConstructor(
                            DRI("kotlin", "Function1"),
                            listOf(
                                TypeConstructor(
                                    DRI("kotlin", "Int"),
                                    emptyList(),
                                    FunctionModifiers.NONE
                                ),
                                TypeConstructor(
                                    DRI("kotlin", "String"),
                                    emptyList(),
                                    FunctionModifiers.NONE
                                )
                            ),
                            FunctionModifiers.FUNCTION
                        )
                    ),
                    FunctionModifiers.FUNCTION
                ),
                simpleParameter(
                    DRI("some", "receiver"),
                    "receiver",
                    TypeConstructor(
                        DRI("kotlin", "Function1"),
                        listOf(
                            TypeConstructor(
                                DRI("kotlin", "Int"),
                                emptyList(),
                                FunctionModifiers.NONE
                            ),
                            TypeConstructor(
                                DRI("kotlin", "String"),
                                emptyList(),
                                FunctionModifiers.NONE
                            )
                        ),
                        FunctionModifiers.FUNCTION
                    )
                ),
                listOf(
                    simpleTypeParameter(
                        Covariance(TypeParameter(DRI("some", "typeparam1"), "T")),
                        listOf(
                            TypeConstructor(
                                DRI("kotlin", "Function1"),
                                listOf(
                                    TypeConstructor(
                                        DRI("kotlin", "Int"),
                                        emptyList(),
                                        FunctionModifiers.NONE
                                    ),
                                    TypeConstructor(
                                        DRI("kotlin", "String"),
                                        emptyList(),
                                        FunctionModifiers.NONE
                                    )
                                ),
                                FunctionModifiers.FUNCTION
                            )
                        )
                    ),
                    simpleTypeParameter(
                        Covariance(TypeParameter(DRI("some", "typeparam1"), "T")),
                        listOf(
                            TypeConstructor(
                                DRI("kotlin", "Function1"),
                                listOf(
                                    TypeConstructor(
                                        DRI("kotlin", "Int"),
                                        emptyList(),
                                        FunctionModifiers.NONE
                                    ),
                                    TypeConstructor(
                                        DRI("kotlin", "String"),
                                        emptyList(),
                                        FunctionModifiers.NONE
                                    )
                                ),
                                FunctionModifiers.FUNCTION
                            )
                        )
                    ),
                )
            )
        )

        assertEquals(after, expected)
    }
}
