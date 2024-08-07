package org.virtuslab.inkuire.engine.impl.model

import com.softwaremill.quicklens._
import org.virtuslab.inkuire.engine.impl.model.TypeName._

sealed trait TypeLike {
  def itid: Option[ITID]
}

case class Type(
  name:             TypeName,
  params:           Seq[Variance] = Seq.empty,
  nullable:         Boolean = false,
  itid:             Option[ITID] = None,
  isVariable:       Boolean = false,
  isStarProjection: Boolean = false,
  isUnresolved:     Boolean = true
) extends TypeLike {
  def ? : Type = this.modify(_.nullable).setTo(true)

  def isGeneric: Boolean = params.nonEmpty

  def asVariable: Type = this.modify(_.isVariable).setTo(true).modify(_.isUnresolved).setTo(false)

  def asConcrete: Type = this.modify(_.isVariable).setTo(false).modify(_.isUnresolved).setTo(false)
}

case class AndType(left: TypeLike, right: TypeLike) extends TypeLike {
  def itid: Option[ITID] = None
}
case class OrType(left: TypeLike, right: TypeLike) extends TypeLike {
  def itid: Option[ITID] = None
}

case class TypeLambda(args: Seq[Type], result: TypeLike) extends TypeLike {
  def itid: Option[ITID] = None
}

object Type {
  implicit class StringTypeOps(str: String) {
    def concreteType: Type = Type(str)
    def typeVariable: Type = Type(str, isVariable = true)
  }
  val StarProjection: Type = Type(TypeName("_"), itid = Some(ITID("_", isParsed = true)), isStarProjection = true)
}
