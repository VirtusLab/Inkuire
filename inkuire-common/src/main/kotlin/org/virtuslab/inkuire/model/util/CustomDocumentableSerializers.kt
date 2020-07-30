package org.virtuslab.inkuire.model.util

import com.google.gson.*
import org.virtuslab.inkuire.model.*
import java.lang.IllegalStateException
import java.lang.reflect.Type

class ClasslikeSerializer : JsonDeserializer<SDClasslike>, JsonSerializer<SDClasslike> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SDClasslike {
        return if (json != null && context != null) {
            val obj = json.asJsonObject
            val toDeserialize = obj.deepCopy().also { it.remove("kind") }
            when(val name = obj.get("kind").asString){ // Casts are there to bypass problems with implicits casts
                "class" -> context.deserialize(toDeserialize, SDClass::class.java) as SDClass
                "annotation" -> context.deserialize(toDeserialize, SDAnnotation::class.java) as SDAnnotation
                "interface" -> context.deserialize(toDeserialize, SDInterface::class.java) as SDInterface
                "enum" -> context.deserialize(toDeserialize, SDEnum::class.java) as SDEnum
                "object" -> context.deserialize(toDeserialize, SDObject::class.java) as SDObject
                "null" -> NullClasslike
                else -> throw IllegalStateException("Cannot deserialize classlike typed as $name")
            }
        } else NullClasslike
    }

    override fun serialize(src: SDClasslike?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if(src != null && context != null) {
            when (src) {
                is SDClass -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("kind", "class") }
                }
                is SDAnnotation -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("kind", "annotation") }
                }
                is SDInterface -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("kind", "interface") }
                }
                is SDEnum -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("kind", "enum") }
                }
                is SDObject -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("kind", "object") }
                }
                is NullClasslike -> JsonObject().also { it.addProperty("kind", "null") }
            }
        } else JsonObject().also { it.addProperty("kind", "null") }
    }
}

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
                is SOtherParameter -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("boundkind", "other") }
                }
                is SNullBound -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("boundkind", "null") }
                }
                is SDynamic -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("boundkind", "dynamic") }
                }
                is SUnresolvedBound -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("boundkind", "unresolvedBound") }
                }
            }
        } else JsonObject().also { it.addProperty("boundkind", "null") }
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SBound {
        return if (json != null && context != null) {
            val obj = json.asJsonObject
            val toDeserialize = obj.deepCopy().also { it.remove("boundkind") }
            when(val name = obj.get("boundkind").asString){ // Casts are there to bypass problems with implicits casts
                "typeconstructor" -> context.deserialize(toDeserialize, STypeConstructor::class.java) as STypeConstructor
                "nullable" -> context.deserialize(toDeserialize, SNullable::class.java) as SNullable
                "primitive" -> context.deserialize(toDeserialize, SPrimitiveJavaType::class.java) as SPrimitiveJavaType
                "other" -> context.deserialize(toDeserialize, SOtherParameter::class.java) as SOtherParameter
                "void" -> context.deserialize(toDeserialize, SVoid::class.java) as SVoid
                "object" -> context.deserialize(toDeserialize, SJavaObject::class.java) as SJavaObject
                "null" -> context.deserialize(toDeserialize, SNullable::class.java) as SNullable
                "dynamic" -> context.deserialize(toDeserialize, SDynamic::class.java) as SDynamic
                "unresolvedBound" -> context.deserialize(toDeserialize, SUnresolvedBound::class.java) as SUnresolvedBound
                else -> throw IllegalStateException("Cannot deserialize bound named $name")
            }
        } else SNullBound
    }
}

class ProjectionSerializer : JsonSerializer<SProjection>,JsonDeserializer<SProjection>{
    //TODO: Get rid of this ext. function. Currently GSON doesn't add bound field when serializing projection idk why
    // and we need to do it explicitly
    private fun SBound.boundKind() = when(this) {
        is STypeConstructor -> "typeconstructor"
        is SNullable -> "nullable"
        is SJavaObject -> "object"
        is SVoid -> "void"
        is SPrimitiveJavaType -> "primitive"
        is SOtherParameter -> "other"
        else -> "null"
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SProjection {
        return if (json != null && context != null) {
            val obj = json.asJsonObject
            val toDeserialize = obj.deepCopy().also { it.remove("kind") }
            when(val name = obj.get("kind").asString) { // Casts are there to bypass problems with implicits casts
                "star" -> context.deserialize(toDeserialize, SStar::class.java) as SStar
                "bound" -> context.deserialize(toDeserialize, SBound::class.java) as SBound
                "variance" -> context.deserialize(toDeserialize, SVariance::class.java) as SVariance
                "null" -> context.deserialize(toDeserialize, SNullProjection::class.java) as SNullProjection
                else -> throw IllegalStateException("Cannot deserialize kind named $name")
            }
        } else SNullProjection
    }

    override fun serialize(src: SProjection?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if(src != null && context != null) {
            when (src) {
                is SStar -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("kind", "star") }
                }
                is SBound -> src.let {
                    val kind = it.boundKind()
                    context.serialize(it).asJsonObject.also {
                        it.addProperty("kind", "bound")
                        it.addProperty("boundkind", kind)
                    }
                }
                is SVariance -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("kind", "variance") }
                }
                is SNullProjection -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("kind", "null") }
                }
            }
        } else JsonObject().also { it.addProperty("kind", "null") }
    }
}
