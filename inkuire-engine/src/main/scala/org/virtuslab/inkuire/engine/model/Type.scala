package org.virtuslab.inkuire.engine.model

import com.softwaremill.quicklens._
import io.scalaland.chimney.dsl._

trait Type {
  def asVariable: Type
  def asConcrete: Type
  def nullable: Boolean
  def ? : Type
}

case class Unresolved(
  name: String,
  nullable: Boolean = false
) extends Type {

  override def asVariable: Type = this.transformInto[TypeVariable]

  override def asConcrete: Type = this.transformInto[ConcreteType]

  override def ? : Type = this.modify(_.nullable).setTo(true)
}

case class ConcreteType(
  name: String,
  nullable: Boolean = false
) extends Type {

  override def asVariable: Type = this.transformInto[TypeVariable]

  override def asConcrete: Type = this

  override def ? : Type = this.modify(_.nullable).setTo(true)
}

case class GenericType(
  base: Type,
  params: Seq[Type]
) extends Type {

  override def asVariable: Type = this.modify(_.base).using(_.asVariable)

  override def asConcrete: Type = this.modify(_.base).using(_.asConcrete)

  override def nullable: Boolean = base.nullable

  override def ? : Type = this.modify(_.base).using(_.?)
}

case class TypeVariable(
  name: String,
  nullable: Boolean = false
) extends Type {

  override def asVariable: Type = this

  override def asConcrete: Type = this.transformInto[ConcreteType]

  override def ? : Type = this.modify(_.nullable).setTo(true)
}

case class FunctionType(
  receiver: Option[Type],
  args: Seq[Type],
  result: Type,
  nullable: Boolean = false
) extends Type {

  override def asVariable: Type = throw new RuntimeException("Operation not allowed!")

  override def asConcrete: Type = this

  override def ? : Type = this.modify(_.nullable).setTo(true)
}

case object StarProjection extends Type {

  override def asVariable: Type = throw new RuntimeException("Operation not allowed!")

  override def asConcrete: Type = throw new RuntimeException("Operation not allowed!")

  override def nullable: Boolean = throw new RuntimeException("Operation not allowed!")

  override def ? : Type = throw new RuntimeException("Operation not allowed!")
}

object Type {
  implicit class StringTypeOps(str: String) {
    def concreteType: ConcreteType = ConcreteType(str)
    def typeVariable: TypeVariable = TypeVariable(str)
    def unresolved: Unresolved = Unresolved(str)
  }
}