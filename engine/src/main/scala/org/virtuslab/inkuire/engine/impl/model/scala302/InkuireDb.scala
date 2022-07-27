package org.virtuslab.inkuire.engine.impl.model.scala302

import io.circe._
import io.circe.generic.auto._
import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser.decode
import io.circe.syntax._
import org.virtuslab.inkuire.engine.impl.model
import org.virtuslab.inkuire.engine.impl.model._
import org.virtuslab.inkuire.engine.api

import scala.util.chaining._

case class Type(
  name:             TypeName,
  params:           Seq[Variance] = Seq.empty,
  nullable:         Boolean = false,
  itid:             Option[ITID] = None,
  isVariable:       Boolean = false,
  isStarProjection: Boolean = false,
  isUnresolved:     Boolean = true
) {
  def toCurrent: model.Type = model.Type(
    name,
    params.map(_.toCurrent),
    nullable,
    itid,
    isVariable,
    isStarProjection,
    isUnresolved
  )
}

object Type {
  implicit val typeDecoder: Decoder[Type] = deriveDecoder
}

sealed trait Variance {
  val typ: Type
  def toCurrent: model.Variance
}

object Variance {
  implicit val varianceDecoder: Decoder[Variance] = (src: HCursor) =>
    for {
      kind <- src.downField("variancekind").as[String]
      parsed <- kind match {
        case "covariance"     => src.value.as[Covariance]
        case "contravariance" => src.value.as[Contravariance]
        case "invariance"     => src.value.as[Invariance]
      }
    } yield parsed
}

case class Covariance(typ: Type) extends Variance {
  def toCurrent: model.Covariance = model.Covariance(typ.toCurrent)
}

case class Contravariance(typ: Type) extends Variance {
  def toCurrent: model.Contravariance = model.Contravariance(typ.toCurrent)
}

case class Invariance(typ: Type) extends Variance {
  def toCurrent: model.Invariance = model.Invariance(typ.toCurrent)
}

case class UnresolvedVariance(typ: Type) extends Variance {
  def toCurrent: model.UnresolvedVariance = model.UnresolvedVariance(typ.toCurrent)
}

case class Signature(
  receiver:  Option[Contravariance],
  arguments: Seq[Contravariance],
  result:    Covariance,
  context:   SignatureContext
) {
  def toCurrent: model.Signature =
    model.Signature(
      receiver.map(_.toCurrent),
      arguments.map(_.toCurrent),
      result.toCurrent,
      context.toCurrent
    )
}

case class SignatureContext(
  vars:        Set[String],
  constraints: Map[String, Seq[Type]]
) {
  def toCurrent: model.SignatureContext =
    model.SignatureContext(
      vars,
      constraints.map {
        case (name, types) => name -> types.map(_.toCurrent)
      }.toMap
    )
}

case class AnnotatedSignature(
  signature:   Signature,
  name:        String,
  packageName: String,
  uri:         String
) {
  def toCurrent: model.AnnotatedSignature =
    model.AnnotatedSignature(
      signature.toCurrent,
      name,
      packageName,
      uri,
      "def"
    )
}

case class InkuireDb(
  functions:           Seq[AnnotatedSignature],
  types:               Map[ITID, (Type, Seq[Type])],
  implicitConversions: Seq[(ITID, Type)]
)

object InkuireDb {

  def toCurrent(inkuireDb: InkuireDb): api.InkuireDb = {
    api.InkuireDb(
      inkuireDb.functions.map(_.toCurrent),
      inkuireDb.types.map {
        case (itid, (tpe, parents)) => itid -> (tpe.toCurrent, parents.map(_.toCurrent))
      },
      inkuireDb.implicitConversions.flatMap {
        case (itid, tpe) =>
          inkuireDb.types.get(itid).filter(_._1.params.isEmpty).map(_._1.toCurrent -> tpe.toCurrent)
      },
      Map.empty
    )
  }

  implicit val itidKeyDecoder: KeyDecoder[ITID] = (str: String) =>
    if (str.startsWith("true=")) ITID.parsed(str.stripPrefix("true=")).pipe(Some.apply)
    else if (str.startsWith("false=")) ITID.external(str.stripPrefix("false=")).pipe(Some.apply)
    else None

  def deserialize(str: String): Either[String, api.InkuireDb] =
    decode[InkuireDb](str).fold(l => Left(l.toString), idb => Right.apply(InkuireDb.toCurrent(idb)))
}
