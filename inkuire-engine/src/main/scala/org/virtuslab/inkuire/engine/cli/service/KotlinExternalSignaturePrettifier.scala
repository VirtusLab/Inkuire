package org.virtuslab.inkuire.engine.cli.service

import org.virtuslab.inkuire.engine.model.Signature
import org.virtuslab.inkuire.engine.model.ExternalSignature
import org.virtuslab.inkuire.engine.model.ConcreteType
import org.virtuslab.inkuire.engine.model.GenericType
import org.virtuslab.inkuire.engine.model.Type
import org.virtuslab.inkuire.engine.model.FunctionType
import org.virtuslab.inkuire.engine.model.TypeVariable

class KotlinExternalSignaturePrettifier {

  def prettify(sgns: Seq[ExternalSignature]): String = {
    sgns.map(doPrettify).map(identity).mkString("\n")
  }

  private def doPrettify(esgn: ExternalSignature): String = {
    s"${esgn.name}: ${prettifySignature(esgn.signature)}"
  }

  private def prettifySignature(sgn: Signature): String = {
    s"${prettifyReceiver(sgn.receiver)}(${prettifyArgs(sgn.arguments)}) -> ${prettifyType(sgn.result)}"
  }

  private def prettifyReceiver(receiver: Option[Type]): String = {
    receiver.fold("")(prettifyType(_) ++ ".")
  }

  private def prettifyArgs(args: Seq[Type]): String = {
    args.map(prettifyType).mkString(", ")
  }

  private def prettifyFunction(f: FunctionType): String = {
    s"${prettifyReceiver(f.receiver)}(${prettifyArgs(f.args)}) -> ${prettifyType(f.result)}"
  }

  private def prettifyType(t: Type): String = {
    t match {
      case ConcreteType(name, nullable) => s"$name${if(nullable) "?" else ""}"
      case TypeVariable(name, nullable) => s"$name${if(nullable) "?" else ""}"
      case GenericType(ConcreteType(name, nullable), params)    => s"$name<${prettifyArgs(params)}>${if(nullable) "?" else ""}"
      case GenericType(TypeVariable(name, nullable), params)    => s"$name<${prettifyArgs(params)}>${if(nullable) "?" else ""}"
      case f: FunctionType              => prettifyFunction(f)
      case _                            => t.toString
    }
  }
}

object KotlinExternalSignaturePrettifier extends KotlinExternalSignaturePrettifier