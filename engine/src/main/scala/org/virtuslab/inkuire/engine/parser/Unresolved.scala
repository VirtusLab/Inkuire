package org.virtuslab.inkuire.engine.parser

import org.virtuslab.inkuire.engine.model.{ConcreteType, DRI, Type, TypeName, TypeVariable, Variance}
import com.softwaremill.quicklens._

private[parser] case class Unresolved(
  name:     TypeName,
  nullable: Boolean = false
) extends Type {
  import io.scalaland.chimney.dsl._

  override def asVariable: Type = this.transformInto[TypeVariable]

  override def asConcrete: Type = this.transformInto[ConcreteType]

  override def params: Seq[Variance] = Seq.empty

  override def dri: Option[DRI] = None

  override def ? : Type = this.modify(_.nullable).setTo(true)

  override def isVariable: Boolean = false
}
