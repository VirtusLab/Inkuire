package org.virtuslab.inkuire.engine.common.service

import cats.data.NonEmptyList
import org.virtuslab.inkuire.engine.common.model._

class ScalaExternalSignaturePrettifier extends SignaturePrettifier {

  override def prettify(sgns: Seq[ExternalSignature]): String =
    sgns.map(doPrettify).map(identity).mkString("\n")

  private def doPrettify(esgn: ExternalSignature): String =
    s"${prettifySignature(esgn.signature)}"

  private def prettifySignature(sgn: Signature): String = {
    s"${prettifyTypeVariables(sgn.context)}" +
      s"${prettifyArgs(sgn.typesWithVariances, " => ")}"
  }

  private def prettifyTypeVariables(context: SignatureContext): String = {
    if (context.vars.isEmpty) ""
    else {
      s"[${context.vars.mkString(", ")}] => "
    }
  }

  private def prettifyArgs(args: Seq[Variance], sep: String = ", "): String =
    args.map(_.typ).map(prettifyType).mkString(sep)

  private def prettifyType(t: Type): String = t match {
    case t: Type if t.isStarProjection => "*"
    case t: Type if t.isGeneric && !t.isVariable && t.name.name.matches("Function.*") =>
      s"(${prettifyArgs(t.params, " => ")})"
    case t: Type if t.isGeneric && !t.isVariable && t.name.name.matches("Tuple.*") =>
      s"(${prettifyArgs(t.params)})"
    case t: Type if t.isGeneric =>
      s"${t.name}[${prettifyArgs(t.params)}]"
    case t: Type => s"${t.name}"
    case _ => t.toString
  }
}
