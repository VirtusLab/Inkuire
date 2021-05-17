package org.virtuslab.inkuire.engine.common.serialization

import io.circe.parser.decode
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import cats.syntax.all._
import org.virtuslab.inkuire.engine.common.model._

object EngineModelSerializers {
  implicit val varianceEncoder: Encoder[Variance] = Encoder.instance {
    case co: Covariance     => Json.obj("variancekind" -> Json.fromString("covariance")).deepMerge(co.asJson)
    case ct: Contravariance => Json.obj("variancekind" -> Json.fromString("contravariance")).deepMerge(ct.asJson)
    case in: Invariance     => Json.obj("variancekind" -> Json.fromString("invariance")).deepMerge(in.asJson)
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

  implicit val itidKeyEncoder: KeyEncoder[ITID] = (id: ITID) => s"${id.isParsed}=${id.uuid}"

  implicit val itidKeyDecoder: KeyDecoder[ITID] = (str: String) =>
    if (str.startsWith("true=")) ITID.parsed(str.stripPrefix("true=")).some
    else if (str.startsWith("false=")) ITID.external(str.stripPrefix("false=")).some
    else None

  def serialize(db: InkuireDb): String = db.asJson.toString

  def deserialize(str: String): Either[String, InkuireDb] = decode[InkuireDb](str).leftMap(_.show)
}
