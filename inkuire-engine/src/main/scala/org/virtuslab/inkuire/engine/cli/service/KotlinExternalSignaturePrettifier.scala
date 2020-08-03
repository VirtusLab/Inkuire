package org.virtuslab.inkuire.engine.cli.service

import org.virtuslab.inkuire.engine.model.{ConcreteType, ExternalSignature, FunctionType, GenericType, Signature, SignatureContext, Type, TypeVariable}

class KotlinExternalSignaturePrettifier {

  def prettify(sgns: Seq[ExternalSignature]): String = {
    sgns.map(doPrettify).map(identity).mkString("\n")
  }

  private def doPrettify(esgn: ExternalSignature): String = {
    s"${esgn.name}: ${prettifySignature(esgn.signature)}"
  }

  private def prettifySignature(sgn: Signature): String = {
    s"${prettifyGenerics(sgn.context)}${prettifyReceiver(sgn.receiver)}(${prettifyArgs(sgn.arguments)}) -> ${prettifyType(sgn.result)}"
  }

  private def prettifyGenerics(context: SignatureContext): String = {
    if(context.vars.isEmpty) ""
    else {
      val consSeq = context.constraints.flatMap{
        case (key,value) => value.map(v => s"$key: ${prettifyType(v)}")
      }
      s"<${consSeq.mkString(", ")}> "
    }
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