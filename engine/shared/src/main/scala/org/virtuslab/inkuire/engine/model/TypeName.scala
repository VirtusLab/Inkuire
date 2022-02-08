package org.virtuslab.inkuire.engine.model

import scala.language.implicitConversions

case class TypeName(name: String) {
  override def hashCode(): Int = name.toLowerCase.hashCode

  override def equals(obj: Any): Boolean = {
    obj match {
      case o: TypeName => this.name.toLowerCase == o.name.toLowerCase
      case _ => false
    }
  }

  override def toString: String = name
}

object TypeName {
  implicit def stringToTypeName(str: String): TypeName = TypeName(str)
}
