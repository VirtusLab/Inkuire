package serialization

import org.jetbrains.dokka.links.DRI
import org.junit.Assert.assertEquals
import org.junit.Test
import org.virtuslab.inkuire.model.AncestryGraph
import org.virtuslab.inkuire.model.SFunctionModifiers
import org.virtuslab.inkuire.model.STypeConstructor
import org.virtuslab.inkuire.plugin.transformers.DefaultDokkaToSerializableModelTransformer.toSerializable
import org.virtuslab.inkuire.plugin.transformers.anyAndNothingAppender

class AnyAndNothingAppenderTest {

    val anyBound = STypeConstructor(
            DRI("kotlin", "Any").toSerializable(),
            emptyList(),
            SFunctionModifiers.NONE
    )

    @Test
    fun `any for plain class`() {

        val driOfClass = DRI("org.virtuslab", "SomeClass").toSerializable()

        val classBound = STypeConstructor(
            driOfClass,
            emptyList(),
            SFunctionModifiers.NONE
        )

        val before = listOf(
            AncestryGraph(
                dri = driOfClass,
                type = classBound,
                superTypes = emptyList()
            )
        )

        val after = before.anyAndNothingAppender()

        val expected = listOf(
            AncestryGraph(
                dri = driOfClass,
                type = classBound,
                superTypes = listOf(anyBound)
            )
        )

        assertEquals(expected, after)
    }

    @Test
    fun `class with supertype`() {

        val driOfClass = DRI("org.virtuslab", "SomeClass").toSerializable()

        val classBound = STypeConstructor(
            driOfClass,
            emptyList(),
            SFunctionModifiers.NONE
        )

        val supertypeBound = STypeConstructor(
                DRI("org.virtuslab", "SuperClass").toSerializable(),
                emptyList(),
                SFunctionModifiers.NONE
        )

        val before = listOf(
                AncestryGraph(
                    dri = driOfClass,
                    type = classBound,
                    superTypes = listOf(supertypeBound)
                )
        )

        val after = before.anyAndNothingAppender()

        val expected = listOf(
                AncestryGraph(
                    dri = driOfClass,
                    type = classBound,
                    superTypes = listOf(supertypeBound)
                )
        )

        assertEquals(expected, after)
    }
}
