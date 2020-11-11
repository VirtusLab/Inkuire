package org.virtuslab.inkuire.engine.common.parser

import org.virtuslab.inkuire.engine.common.model.ITID
import com.softwaremill.quicklens._
import org.virtuslab.inkuire.engine.common.model.{ConcreteType, ITID, Type, TypeName, TypeVariable, Variance}

private[parser] case class Unresolved(
  name:     TypeName,
  nullable: Boolean = false
) extends Type {
  import io.scalaland.chimney.dsl._

  override def asVariable: Type = this.transformInto[TypeVariable]

  override def asConcrete: Type = this.transformInto[ConcreteType]

  override def params: Seq[Variance] = Seq.empty

  override def itid: Option[ITID] = None

  override def ? : Type = this.modify(_.nullable).setTo(true)

  override def isVariable: Boolean = false
}
