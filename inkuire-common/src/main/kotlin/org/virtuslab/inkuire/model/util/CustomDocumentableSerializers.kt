package org.virtuslab.inkuire.model.util

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import org.virtuslab.inkuire.model.*
import java.lang.reflect.Type


class BoundSerializer : JsonSerializer<SBound>, JsonDeserializer<SBound>{
    override fun serialize(src: SBound?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if(src != null && context != null) {
            when (src) {
                is STypeConstructor -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("boundkind", "typeconstructor") }
                }
                is SNullable -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("boundkind", "nullable") }
                }
                is SJavaObject -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("boundkind", "object") }
                }
                is SVoid -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("boundkind", "void") }
                }
                is SPrimitiveJavaType -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("boundkind", "primitive") }
                }
                is STypeParameter -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("boundkind", "typeparameter") }
                }
                is SDynamic -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("boundkind", "dynamic") }
                }
                is SUnresolvedBound -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("boundkind", "unresolvedBound") }
                }
            }
        } else throw IllegalStateException("Cannot serialize bound named ${if (src != null) src::class.simpleName else "null"}")
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SBound {
        return if (json != null && context != null) {
            val obj = json.asJsonObject
            val toDeserialize = obj.deepCopy().also { it.remove("boundkind") }
            when(val name = obj.get("boundkind").asString){ // Casts are there to bypass problems with implicits casts
                "typeconstructor" -> context.deserialize(toDeserialize, STypeConstructor::class.java) as STypeConstructor
                "nullable" -> context.deserialize(toDeserialize, SNullable::class.java) as SNullable
                "primitive" -> context.deserialize(toDeserialize, SPrimitiveJavaType::class.java) as SPrimitiveJavaType
                "typeparameter" -> context.deserialize(toDeserialize, STypeParameter::class.java) as STypeParameter
                "void" -> context.deserialize(toDeserialize, SVoid::class.java) as SVoid
                "object" -> context.deserialize(toDeserialize, SJavaObject::class.java) as SJavaObject
                "dynamic" -> context.deserialize(toDeserialize, SDynamic::class.java) as SDynamic
                "unresolvedBound" -> context.deserialize(toDeserialize, SUnresolvedBound::class.java) as SUnresolvedBound
                else -> throw IllegalStateException("Cannot deserialize bound named $name")
            }
        } else throw IllegalStateException("Cannot deserialize bound when json is $json and context is $context")
    }
}

class ProjectionSerializer : JsonSerializer<SProjection>,JsonDeserializer<SProjection>{

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SProjection {
        return if (json != null && context != null) {
            val obj = json.asJsonObject
            val toDeserialize = obj.deepCopy().also { it.remove("kind") }
            when(val name = obj.get("kind").asString) { // Casts are there to bypass problems with implicits casts
                "star" -> context.deserialize(toDeserialize, SStar::class.java) as SStar
                "bound" -> context.deserialize(toDeserialize, SBound::class.java) as SBound
                "variance" -> context.deserialize(toDeserialize, SVariance::class.java) as SVariance
                else -> throw IllegalStateException("Cannot deserialize projection named $name")
            }
        } else throw IllegalStateException("Cannot deserialize bound when json is $json and context is $context")
    }

    val sboundType = object : TypeToken<SBound>() {}.type

    override fun serialize(src: SProjection?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if(src != null && context != null) {
            when (src) {
                is SStar -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("kind", "star") }
                }
                is SBound -> src.let {
                    context.serialize(it, sboundType).asJsonObject.also { it.addProperty("kind", "bound") }
                }
                is SVariance -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("kind", "variance") }
                }
            }
        } else throw IllegalStateException("Cannot serialize projection named ${if (src != null) src::class.simpleName else "null"}")
    }
}
