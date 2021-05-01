package org.virtuslab.inkuire.engine.common.model

import com.softwaremill.quicklens._
import TypeName._

case class Type(
  name: TypeName,
  params: Seq[Variance] = Seq.empty,
  nullable: Boolean = false,
  itid: Option[ITID] = None,
  isVariable: Boolean = false,
  isStarProjection: Boolean = false,
  isUnresolved: Boolean = true
) {
  def ? : Type = this.modify(_.nullable).setTo(true)

  def isGeneric: Boolean = params.nonEmpty

  def asVariable: Type = this.modify(_.isVariable).setTo(true)

  def asConcrete: Type = this.modify(_.isVariable).setTo(false)
}

object Type {
  implicit class StringTypeOps(str: String) {
    def concreteType: Type = Type(str)
    def typeVariable: Type = Type(str, isVariable = true)
  }
  val StarProjection = Type(TypeName("_"), isStarProjection = true)
}
