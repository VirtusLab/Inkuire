package org.virtuslab.inkuire.engine.parser.model

trait Type
case class ConcreteType(name: String) extends Type
case class GenericType(name: String, params: Seq[Type]) extends Type

object Type {
  implicit class StringTypeOps(str: String) {
    def concreteType: ConcreteType = ConcreteType(str)
  }
}
