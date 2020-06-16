package org.virtuslab.inkuire.model.util

import com.google.gson.*
import org.virtuslab.inkuire.model.*
import java.lang.reflect.Type

class ClasslikeSerializer : JsonDeserializer<SDClasslike>, JsonSerializer<SDClasslike> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SDClasslike {
        return if (json != null && context != null) {
            val obj = json.asJsonObject
            val toDeserialize = obj.deepCopy().also { it.remove("kind") }
            when(obj.get("kind").asString){
                "class" -> context.deserialize(toDeserialize, SDClass::class.java)
                "annotation" -> context.deserialize(toDeserialize, SDAnnotation::class.java)
                "interface" -> context.deserialize(toDeserialize, SDInterface::class.java)
                "enum" -> context.deserialize(toDeserialize, SDEnum::class.java)
                "object" -> context.deserialize(toDeserialize, SDObject::class.java)
                else -> NullClasslike()
            }
        } else NullClasslike()
    }

    override fun serialize(src: SDClasslike?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if(src != null && context != null) {
            when (src) {
                is SDClass -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("kind", "class") }
                }
                is SDAnnotation -> src.let {
                    context?.serialize(it).asJsonObject.also { it.addProperty("kind", "annotation") }
                }
                is SDInterface -> src.let {
                    context?.serialize(it).asJsonObject.also { it.addProperty("kind", "interface") }
                }
                is SDEnum -> src.let {
                    context?.serialize(it).asJsonObject.also { it.addProperty("kind", "enum") }
                }
                is SDObject -> src.let {
                    context?.serialize(it).asJsonObject.also { it.addProperty("kind", "object") }
                }
                is NullClasslike -> JsonObject().also { it.addProperty("kind", "null") }
            }
        } else return JsonObject().also { it.addProperty("kind", "null") }
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
                    context?.serialize(it).asJsonObject.also { it.addProperty("boundkind", "nullable") }
                }
                is SJavaObject -> src.let {
                    context?.serialize(it).asJsonObject.also { it.addProperty("boundkind", "object") }
                }
                is SVoid -> src.let {
                    context?.serialize(it).asJsonObject.also { it.addProperty("boundkind", "void") }
                }
                is SPrimitiveJavaType -> src.let {
                    context?.serialize(it).asJsonObject.also { it.addProperty("boundkind", "primitive") }
                }
                is SOtherParameter -> src.let {
                    context?.serialize(it).asJsonObject.also { it.addProperty("boundkind", "other") }
                }
                else -> src.let {
                    context?.serialize(it).asJsonObject.also { it.addProperty("boundkind", "null") }
                }
            }
        } else return JsonObject().also { it.addProperty("boundkind", "null") }
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SBound {
        return if (json != null && context != null) {
            val obj = json.asJsonObject
            val toDeserialize = obj.deepCopy().also { it.remove("boundkind") }
            when(obj.get("boundkind").asString){
                "typeconstructor" -> context.deserialize(toDeserialize, STypeConstructor::class.java)
                "nullable" -> context.deserialize(toDeserialize, SNullable::class.java)
                "primitive" -> context.deserialize(toDeserialize, SPrimitiveJavaType::class.java)
                "other" -> context.deserialize(toDeserialize, SOtherParameter::class.java)
                "void" -> context.deserialize(toDeserialize, SVoid::class.java)
                "object" -> context.deserialize(toDeserialize, SJavaObject::class.java)
                else -> NullBound()
            }
        } else NullBound()
    }
}

class ProjectionSerializer : JsonSerializer<SProjection>,JsonDeserializer<SProjection>{
    //TODO: Get rid of this ext. function. Currently GSON doesn't add bound field when serializing projection idk why
    // and we need to do it explicitly
    private fun SBound.boundKind() = when(this){
        is STypeConstructor -> "typeconstructor"
        is SNullable -> "nullable"
        is SJavaObject -> "object"
        is SVoid -> "void"
        is SPrimitiveJavaType -> "primitive"
        is SOtherParameter ->  "other"
        else -> "null"
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SProjection {
        return if (json != null && context != null) {
            val obj = json.asJsonObject
            val toDeserialize = obj.deepCopy().also { it.remove("kind") }
            when(obj.get("kind").asString){
                "star" -> context.deserialize(toDeserialize, SStar::class.java)
                "bound" -> context.deserialize(toDeserialize, SBound::class.java)
                "variance" -> context.deserialize(toDeserialize, SVariance::class.java)
                else -> NullProjection()
            }
        } else NullProjection()
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
                else -> src.let {
                    context.serialize(it).asJsonObject.also { it.addProperty("kind", "null") }
                }
            }
        } else return JsonObject().also { it.addProperty("kind", "null") }
    }
}
