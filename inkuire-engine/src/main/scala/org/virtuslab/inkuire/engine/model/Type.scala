package org.virtuslab.inkuire.engine.model

import com.softwaremill.quicklens._
import TypeName._

trait Type {
  def asVariable: Type
  def asConcrete: Type
  def name:       TypeName
  def nullable:   Boolean
  def params:     Seq[Type]
  def dri:        Option[DRI]
  def ?         : Type
}

case class ConcreteType(
  name:     TypeName,
  nullable: Boolean = false,
  dri:      Option[DRI] = None
) extends Type {
  import io.scalaland.chimney.dsl._

  override def asVariable: Type = this.transformInto[TypeVariable]

  override def asConcrete: Type = this

  override def params: Seq[Type] = Seq.empty

  override def ? : Type = this.modify(_.nullable).setTo(true)
}

case class GenericType(
  base:   Type,
  params: Seq[Type]
) extends Type {

  override def asVariable: Type = this.modify(_.base).using(_.asVariable)

  override def asConcrete: Type = this.modify(_.base).using(_.asConcrete)

  override def nullable: Boolean = base.nullable

  override def name: TypeName = base.name

  override def dri: Option[DRI] = base.dri

  override def ? : Type = this.modify(_.base).using(_.?)
}

case class TypeVariable(
  name:     TypeName,
  nullable: Boolean = false,
  dri:      Option[DRI] = None
) extends Type {
  import io.scalaland.chimney.dsl._

  override def asVariable: Type = this

  override def asConcrete: Type = this.transformInto[ConcreteType]

  override def params: Seq[Type] = Seq.empty

  override def ? : Type = this.modify(_.nullable).setTo(true)
  // TODO: Issue #28
  override def equals(obj: Any): Boolean = obj match {
    case t: TypeVariable => t.nullable == this.nullable
    case _ => false
  }
}

case object StarProjection extends Type {

  override def asVariable: Type = throw new RuntimeException("Operation not allowed!")

  override def asConcrete: Type = throw new RuntimeException("Operation not allowed!")

  override def nullable: Boolean = throw new RuntimeException("Operation not allowed!")

  override def name: TypeName = "*"

  override def params: Seq[Type] = Seq.empty

  override def dri: Option[DRI] = None //TODO not sure

  override def ? : Type = throw new RuntimeException("Operation not allowed!")
}

object Type {
  implicit class StringTypeOps(str: String) {
    def concreteType: ConcreteType = ConcreteType(str)
    def typeVariable: TypeVariable = TypeVariable(str)
  }
}
