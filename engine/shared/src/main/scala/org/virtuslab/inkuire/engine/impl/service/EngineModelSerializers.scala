package org.virtuslab.inkuire.engine.impl.service

import io.circe._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import org.virtuslab.inkuire.engine.impl.model._
import org.virtuslab.inkuire.engine.api.InkuireDb

import scala.util.chaining._

object EngineModelSerializers {
  implicit val varianceEncoder: Encoder[Variance] = Encoder.instance {
    case co: Covariance         => Json.obj("variancekind" -> Json.fromString("covariance")).deepMerge(co.asJson)
    case ct: Contravariance     => Json.obj("variancekind" -> Json.fromString("contravariance")).deepMerge(ct.asJson)
    case in: Invariance         => Json.obj("variancekind" -> Json.fromString("invariance")).deepMerge(in.asJson)
    case un: UnresolvedVariance => Json.obj("variancekind" -> Json.fromString("unresolved")).deepMerge(un.asJson)
  }

  implicit val varianceDecoder: Decoder[Variance] = (src: HCursor) =>
    for {
      kind <- src.downField("variancekind").as[String]
      parsed <- kind match {
        case "covariance"     => src.value.as[Covariance]
        case "contravariance" => src.value.as[Contravariance]
        case "invariance"     => src.value.as[Invariance]
      }
    } yield parsed

  implicit val typelikeEncoder: Encoder[TypeLike] = Encoder.instance {
    case t: Type       => Json.obj("typelikekind" -> Json.fromString("type")).deepMerge(t.asJson)
    case t: AndType    => Json.obj("typelikekind" -> Json.fromString("andtype")).deepMerge(t.asJson)
    case t: OrType     => Json.obj("typelikekind" -> Json.fromString("ortype")).deepMerge(t.asJson)
    case t: TypeLambda => Json.obj("typelikekind" -> Json.fromString("typelambda")).deepMerge(t.asJson)
  }

  implicit val typelikeDecoder: Decoder[TypeLike] = (src: HCursor) =>
    for {
      kind <- src.downField("typelikekind").as[String]
      parsed <- kind match {
        case "type"       => src.value.as[Type]
        case "andtype"    => src.value.as[AndType]
        case "ortype"     => src.value.as[OrType]
        case "typelambda" => src.value.as[TypeLambda]
      }
    } yield parsed

  implicit val itidKeyEncoder: KeyEncoder[ITID] = (id: ITID) => s"${id.isParsed}=${id.uuid}"

  implicit val itidKeyDecoder: KeyDecoder[ITID] = (str: String) =>
    if (str.startsWith("true=")) ITID.parsed(str.stripPrefix("true=")).pipe(Some.apply)
    else if (str.startsWith("false=")) ITID.external(str.stripPrefix("false=")).pipe(Some.apply)
    else None

  def serialize(db: InkuireDb): String = db.asJson.toString

  def deserialize(str: String): Either[String, InkuireDb] =
    decode[InkuireDb](str).fold(l => Left(l.toString), Right.apply)
}
