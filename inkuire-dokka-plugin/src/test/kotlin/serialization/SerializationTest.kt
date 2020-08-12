package serialization

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.*
import org.junit.Test
import org.virtuslab.inkuire.model.util.CustomGson
import org.virtuslab.inkuire.plugin.transformers.DefaultDokkaToSerializableModelTransformer.toSerializable

class SerializationTest {

    private val gson = CustomGson.instance

    @Test
    fun `type parameter`() {
        val input = TypeParameter(DRI("package", "Class"), "T").toSerializable()
        val actual = gson.toJson(input)
        val expect = """"""
        assert(expect == actual)
    }

    @Test
    fun `star`() {
        val input = Star.toSerializable()
        val actual = gson.toJson(input)
        val expect = """{"declarationDRI":{"packageName":"package","classNames":"Class","target":{}},"name":"T"}"""
        assert(expect == actual)
    }

    @Test
    fun `type constructor`() {

        val input = TypeConstructor(DRI("package", "Class"), emptyList(), FunctionModifiers.NONE).toSerializable()
    }

    @Test
    fun `nullable`() {

        val input = Nullable(UnresolvedBound("placeholder")).toSerializable()
    }

    @Test
    fun `variance`() {

        val input = Variance(Variance.Kind.In, UnresolvedBound("placeholder")).toSerializable()
    }

    @Test
    fun `primitive java type`() {

        val input = PrimitiveJavaType("int").toSerializable()
    }


    @Test
    fun `void`() {

        val input = Void.toSerializable()
    }

    @Test
    fun `java object`() {

        val input = JavaObject.toSerializable()
    }

    @Test
    fun `dynamic`() {

        val input = Dynamic.toSerializable()
    }

    @Test
    fun `unresolved bound`() {

        val input = UnresolvedBound("placeholder").toSerializable()
    }
}