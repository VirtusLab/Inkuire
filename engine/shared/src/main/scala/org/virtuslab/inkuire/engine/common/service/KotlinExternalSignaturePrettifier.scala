package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.api._
import org.virtuslab.inkuire.engine.common.model._

class KotlinExternalSignaturePrettifier extends BaseSignaturePrettifier {

  override def prettify(sgns: Seq[ExternalSignature]): String =
    sgns.map(doPrettify).map(identity).mkString("\n")

  private def doPrettify(esgn: ExternalSignature): String =
    s"${prettifySignature(esgn.signature)}"

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

  private def prettifyType(t: TypeLike): String = {
    t match {
      case t: Type if t.isStarProjection => "*"
      case t: Type if t.isGeneric && !t.isVariable && t.name.name.matches("Function.*") =>
        prettifyFunction(t.params.toList, t.nullable)
      case t: Type if t.isGeneric =>
        s"${t.name}<${prettifyArgs(t.params)}>${if (t.nullable) "?" else ""}"
      case t: Type => s"${t.name}${if (t.nullable) "?" else ""}"
      case AndType(left, right) =>
        "(" + prettifyType(left) + " & " + prettifyType(right) + ")"
      case OrType(left, right) =>
        "(" + prettifyType(left) + " | " + prettifyType(right) + ")"
      case _ => t.toString
    }
  }

  private def prettifyFunction(params: List[Variance], nullable: Boolean): String = {
    val prettifiedFunction = s"(${prettifyArgs(params.init)}) -> ${prettifyType(params.last.typ)}"
    if (nullable) {
      s"($prettifiedFunction)?"
    } else prettifiedFunction
  }
}
