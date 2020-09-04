package org.virtuslab.inkuire.engine.service

import org.virtuslab.inkuire.engine.model._

class KotlinExternalSignaturePrettifier extends SignaturePrettifier {

  override def prettify(sgns: Seq[ExternalSignature]): String =
    sgns.map(doPrettify).map(identity).mkString("\n")

  private def doPrettify(esgn: ExternalSignature): String =
    s"${esgn.name}: ${prettifySignature(esgn.signature)}"

  private def prettifySignature(sgn: Signature): String = {
    s"${prettifyTypeVariables(sgn.context)}" +
      s"${prettifyReceiver(sgn.receiver)}(${prettifyArgs(sgn.arguments)}) -> ${prettifyType(sgn.result.typ)}" +
      s"${prettifyTypeVariableConstraints(sgn.context)}"
  }

  private def prettifyTypeVariables(context: SignatureContext): String = {
    if (context.vars.isEmpty) ""
    else {
      s"<${context.vars.mkString(", ")}> "
    }
  }

  private def prettifyTypeVariableConstraints(context: SignatureContext): String = {
    if (context.constraints.isEmpty || context.constraints.values.flatten.isEmpty) ""
    else {
      val constraints = context.constraints.flatMap {
        case (key, value) => value.map(v => s"$key: ${prettifyType(v)}")
      }
      s" where ${constraints.mkString(", ")}"
    }
  }

  private def prettifyReceiver(receiver: Option[Variance]): String =
    receiver.fold("")(v => prettifyType(v.typ) ++ ".")

  private def prettifyArgs(args: Seq[Variance]): String =
    args.map(_.typ).map(prettifyType).mkString(", ")

  private def prettifyType(t: Type): String = {
    t match {
      case ConcreteType(name, nullable, _) => s"$name${if (nullable) "?" else ""}"
      case TypeVariable(name, nullable, _) => s"$name${if (nullable) "?" else ""}"
      case GenericType(ConcreteType(name, nullable, _), params) =>
        s"$name<${prettifyArgs(params)}>${if (nullable) "?" else ""}"
      case GenericType(TypeVariable(name, nullable, _), params) =>
        s"$name<${prettifyArgs(params)}>${if (nullable) "?" else ""}"
      case _ => t.toString
    }
  }
}
