package org.virtuslab.inkuire.model.util

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import org.virtuslab.inkuire.model.*
import java.lang.reflect.Type

class BoundSerializer : JsonSerializer<SBound>, JsonDeserializer<SBound> {
    override fun serialize(src: SBound?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if (src != null && context != null) {
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
            when (val name = obj.get("boundkind").asString) {
                "typeconstructor" -> context.deserialize<STypeConstructor>(toDeserialize, STypeConstructor::class.java)
                "nullable" -> context.deserialize<SNullable>(toDeserialize, SNullable::class.java)
                "primitive" -> context.deserialize<SPrimitiveJavaType>(toDeserialize, SPrimitiveJavaType::class.java)
                "typeparameter" -> context.deserialize<STypeParameter>(toDeserialize, STypeParameter::class.java)
                "void" -> context.deserialize<SVoid>(toDeserialize, SVoid::class.java)
                "object" -> context.deserialize<SJavaObject>(toDeserialize, SJavaObject::class.java)
                "dynamic" -> context.deserialize<SDynamic>(toDeserialize, SDynamic::class.java)
                "unresolvedBound" -> context.deserialize<SUnresolvedBound>(toDeserialize, SUnresolvedBound::class.java)
                else -> throw IllegalStateException("Cannot deserialize bound named $name")
            }
        } else throw IllegalStateException("Cannot deserialize bound when json is $json and context is $context")
    }
}

class VarianceSerializer : JsonSerializer<SVariance<*>>, JsonDeserializer<SVariance<*>> {

    val scontravarianceType = object : TypeToken<SContravariance<SBound>>() {}.type
    val scovarianceType = object : TypeToken<SCovariance<SBound>>() {}.type
    val sinvarianceType = object : TypeToken<SInvariance<SBound>>() {}.type

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SVariance<*> {
        return if (json != null && context != null) {
            val obj = json.asJsonObject
            val toDeserialize = obj.deepCopy().also { it.remove("variancekind") }
            when (val name = obj.get("variancekind").asString) {
                "contravariance" -> context.deserialize<SContravariance<*>>(toDeserialize, scontravarianceType)
                "covariance" -> context.deserialize<SCovariance<*>>(toDeserialize, scovarianceType)
                "invariance" -> context.deserialize<SInvariance<*>>(toDeserialize, sinvarianceType)
                else -> throw IllegalStateException("Cannot deserialize variance named $name")
            }
        } else throw IllegalStateException("Cannot deserialize variance when json is $json and context is $context")
    }

    override fun serialize(src: SVariance<*>?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if (src != null && context != null) {
            when (src) {
                is SContravariance -> src.let {
                    context.serialize(it, scontravarianceType).asJsonObject.also { it.addProperty("variancekind", "contravariance") }
                }
                is SCovariance -> src.let {
                    context.serialize(it, scovarianceType).asJsonObject.also { it.addProperty("variancekind", "covariance") }
                }
                is SInvariance -> src.let {
                    context.serialize(it, sinvarianceType).asJsonObject.also { it.addProperty("variancekind", "invariance") }
                }
            }
        } else throw IllegalStateException("Cannot serialize variance named ${if (src != null) src::class.simpleName else "null"}")
    }
}

class ProjectionSerializer : JsonSerializer<SProjection>, JsonDeserializer<SProjection> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SProjection {
        return if (json != null && context != null) {
            val obj = json.asJsonObject
            val toDeserialize = obj.deepCopy().also { it.remove("projectionkind") }
            when (val name = obj.get("projectionkind").asString) { // Casts are there to bypass problems with implicits casts
                "star" -> context.deserialize<SStar>(toDeserialize, SStar::class.java)
                "bound" -> context.deserialize<SBound>(toDeserialize, SBound::class.java)
                "variance" -> context.deserialize<SVariance<*>>(toDeserialize, SVariance::class.java)
                else -> throw IllegalStateException("Cannot deserialize projection named $name")
            }
        } else throw IllegalStateException("Cannot deserialize proejction when json is $json and context is $context")
    }

    val sboundType = object : TypeToken<SBound>() {}.type
    val svarianceType = object : TypeToken<SVariance<*>>() {}.type

    override fun serialize(src: SProjection?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if (src != null && context != null) {
            when (src) {
                is SStar -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("projectionkind", "star") }
                }
                is SBound -> src.let {
                    context.serialize(it, sboundType).asJsonObject.also { it.addProperty("projectionkind", "bound") }
                }
                is SVariance<*> -> src.let {
                    context.serialize(it, svarianceType).asJsonObject.also { it.addProperty("projectionkind", "variance") }
                }
            }
        } else throw IllegalStateException("Cannot serialize projection named ${if (src != null) src::class.simpleName else "null"}")
    }
}
