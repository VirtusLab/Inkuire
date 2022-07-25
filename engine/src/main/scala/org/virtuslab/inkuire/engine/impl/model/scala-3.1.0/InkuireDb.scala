package org.virtuslab.inkuire.engine.impl.model.`scala-3.1.0`

import io.circe._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import org.virtuslab.inkuire.engine.impl.model
import org.virtuslab.inkuire.engine.impl.model._
import org.virtuslab.inkuire.engine.api.{ InkuireDb => IDB }

import scala.util.chaining._

case class InkuireDb(
  functions:           Seq[AnnotatedSignature],
  types:               Map[ITID, (Type, Seq[Type])],
  implicitConversions: Seq[(ITID, Type)]
)

object InkuireDb {
  def toCurrent(inkuireDb: InkuireDb): IDB = {
    IDB(
      inkuireDb.functions,
      inkuireDb.types,
      inkuireDb.implicitConversions.flatMap {
        case (itid, tpe) =>
          inkuireDb.types.get(itid).filter(_._1.params.isEmpty).map(_._1 -> tpe)
      },
      Map.empty
    )
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

  implicit val itidKeyDecoder: KeyDecoder[ITID] = (str: String) =>
    if (str.startsWith("true=")) ITID.parsed(str.stripPrefix("true=")).pipe(Some.apply)
    else if (str.startsWith("false=")) ITID.external(str.stripPrefix("false=")).pipe(Some.apply)
    else None

  def deserialize(str: String): Either[String, IDB] =
    decode[InkuireDb](str).fold(l => Left(l.toString), idb => Right.apply(InkuireDb.toCurrent(idb)))
}

