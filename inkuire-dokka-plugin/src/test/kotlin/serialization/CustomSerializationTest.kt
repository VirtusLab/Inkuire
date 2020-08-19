package serialization

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.virtuslab.inkuire.model.SBound
import org.virtuslab.inkuire.model.SProjection
import org.virtuslab.inkuire.model.util.CustomGson
import org.virtuslab.inkuire.plugin.transformers.DefaultDokkaToSerializableModelTransformer.toSerializable

class CustomSerializationTest {

    private val gson = CustomGson.instance

    @Test
    fun `type parameter as bound`() {
        val input: SBound = TypeParameter(DRI("package", "Class"), "T").toSerializable()
        val actual = gson.toJson(input, SBound::class.java)
        val expect = """{"dri":{"packageName":"package","className":"Class","original":"package/Class///PointingToDeclaration/"},"name":"T","boundkind":"typeparameter"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `type parameter as projection`() {
        val input: SBound = TypeParameter(DRI("package", "Class"), "T").toSerializable()
        val actual = gson.toJson(input, SProjection::class.java)
        val expect = """{"dri":{"packageName":"package","className":"Class","original":"package/Class///PointingToDeclaration/"},"name":"T","boundkind":"typeparameter","kind":"bound"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `star`() {
        val input = Star.toSerializable()
        val actual = gson.toJson(input, SProjection::class.java)
        val expect = """{"kind":"star"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `type constructor as bound`() {
        val input = TypeConstructor(DRI("package", "Class"), emptyList(), FunctionModifiers.NONE).toSerializable()
        val actual = gson.toJson(input, SBound::class.java)
        val expect = """{"dri":{"packageName":"package","className":"Class","original":"package/Class///PointingToDeclaration/"},"projections":[],"modifier":"NONE","boundkind":"typeconstructor"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `type constructor as projection`() {
        val input = TypeConstructor(DRI("package", "Class"), emptyList(), FunctionModifiers.NONE).toSerializable()
        val actual = gson.toJson(input, SProjection::class.java)
        val expect = """{"dri":{"packageName":"package","className":"Class","original":"package/Class///PointingToDeclaration/"},"projections":[],"modifier":"NONE","boundkind":"typeconstructor","kind":"bound"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `nullable as bound`() {
        val input = Nullable(UnresolvedBound("placeholder")).toSerializable()
        val actual = gson.toJson(input, SBound::class.java)
        val expect = """{"inner":{"name":"placeholder","boundkind":"unresolvedBound"},"boundkind":"nullable"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `nullable as projection`() {
        val input = Nullable(UnresolvedBound("placeholder")).toSerializable()
        val actual = gson.toJson(input, SProjection::class.java)
        val expect = """{"inner":{"name":"placeholder","boundkind":"unresolvedBound"},"boundkind":"nullable","kind":"bound"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `variance`() {
        val input = Variance(Variance.Kind.In, UnresolvedBound("placeholder")).toSerializable()
        val actual = gson.toJson(input, SProjection::class.java)
        val expect = """{"kind":"variance","inner":{"name":"placeholder","boundkind":"unresolvedBound"}}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `primitive java type as bound`() {
        val input = PrimitiveJavaType("int").toSerializable()
        val actual = gson.toJson(input, SBound::class.java)
        val expect = """{"name":"int","boundkind":"primitive"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `primitive java type as projection`() {
        val input = PrimitiveJavaType("int").toSerializable()
        val actual = gson.toJson(input, SProjection::class.java)
        val expect = """{"name":"int","boundkind":"primitive","kind":"bound"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `void as bound`() {
        val input = Void.toSerializable()
        val actual = gson.toJson(input, SBound::class.java)
        val expect = """{"boundkind":"void"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `void as projection`() {
        val input = Void.toSerializable()
        val actual = gson.toJson(input, SProjection::class.java)
        val expect = """{"boundkind":"void","kind":"bound"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `java object as bound`() {
        val input = JavaObject.toSerializable()
        val actual = gson.toJson(input, SBound::class.java)
        val expect = """{"boundkind":"object"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `java object as projection`() {
        val input = JavaObject.toSerializable()
        val actual = gson.toJson(input, SProjection::class.java)
        val expect = """{"boundkind":"object","kind":"bound"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `dynamic as bound`() {
        val input = Dynamic.toSerializable()
        val actual = gson.toJson(input, SBound::class.java)
        val expect = """{"boundkind":"dynamic"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `dynamic as projection`() {
        val input = Dynamic.toSerializable()
        val actual = gson.toJson(input, SProjection::class.java)
        val expect = """{"boundkind":"dynamic","kind":"bound"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `unresolved bound as bound`() {
        val input = UnresolvedBound("placeholder").toSerializable()
        val actual = gson.toJson(input, SBound::class.java)
        val expect = """{"name":"placeholder","boundkind":"unresolvedBound"}"""
        assertEquals(expect, actual)
    }

    @Test
    fun `unresolved bound as projection`() {
        val input = UnresolvedBound("placeholder").toSerializable()
        val actual = gson.toJson(input, SProjection::class.java)
        val expect = """{"name":"placeholder","boundkind":"unresolvedBound","kind":"bound"}"""
        assertEquals(expect, actual)
    }
}