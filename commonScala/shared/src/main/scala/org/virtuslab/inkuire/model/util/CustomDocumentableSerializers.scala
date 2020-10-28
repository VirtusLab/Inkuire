package org.virtuslab.inkuire.model.util

import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.auto._, io.circe.syntax._
import org.virtuslab.inkuire.model._
import java.lang.reflect.Type

object Deserializer {

  implicit val decodeBound: Decoder[SBound] = (src: HCursor) =>
    for {
      name <- src.downField("boundkind").as[String]
      parsed <- name match {
        case "typeconstructor" => src.value.as[STypeConstructor]
        case "nullable"        => src.value.as[SNullable]
        case "primitive"       => src.value.as[SPrimitiveJavaType]
        case "typeparameter"   => src.value.as[STypeParameter]
        case "void"            => src.value.as[SVoid.type]
        case "object"          => src.value.as[SJavaObject.type]
        case "dynamic"         => src.value.as[SDynamic.type]
        case "unresolvedBound" => src.value.as[SUnresolvedBound]
      }
    } yield parsed

  implicit val decodeVariance: Decoder[SVariance] = (src: HCursor) =>
    for {
      name <- src.downField("variancekind").as[String]
      parsed <- name match {
        case "contravariance" => src.value.as[SContravariance]
        case "covariance"     => src.value.as[SCovariance]
        case "invariance"     => src.value.as[SInvariance]
      }
    } yield parsed

  implicit val decodeProjection: Decoder[SProjection] = (src: HCursor) =>
    for {
      name <- src.downField("projectionkind").as[String]
      parsed <- name match {
        case "star"     => src.value.as[SStar.type]
        case "bound"    => decodeBound(src)
        case "variance" => decodeVariance(src)
      }
    } yield parsed

  implicit val decodeFunctionModifiers: Decoder[SFunctionModifiers] = (src: HCursor) =>
    for {
      name <- src.as[String]
    } yield
      name match {
        case "NONE"      => SFunctionModifiers.NONE
        case "FUNCTION"  => SFunctionModifiers.FUNCTION
        case "EXTENSION" => SFunctionModifiers.EXTENSION
    }

}
