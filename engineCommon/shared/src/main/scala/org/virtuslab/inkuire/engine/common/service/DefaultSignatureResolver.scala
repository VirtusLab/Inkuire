package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model._
import com.softwaremill.quicklens._
import cats.implicits._

class DefaultSignatureResolver(ancestryGraph: Map[DRI, (Type, Seq[Type])])
  extends BaseSignatureResolver
  with VarianceOps {

  val parsedDriPrefix = "iri-" // IRI stands for Inkuire Resource Identifier
  // TODO Consider reporting errors for specific types
  override def resolve(parsed: Signature): ResolveResult = ResolveResult(resolveAllPossibleSignatures(parsed))

  private def resolveAllPossibleSignatures(signature: Signature): Seq[Signature] = {
    for {
      receiver <- signature.receiver
        .fold[Seq[Option[Type]]](Seq(None))(r => resolvePossibleTypes(r.typ).map(_.some))
        .map(_.map(Contravariance))
      args <- resolveMultipleTypes(signature.arguments.map(_.typ)).map(_.map(Contravariance))
      result <- resolvePossibleTypes(signature.result.typ).map(Covariance)
      constraints = signature.context.constraints.view
        .mapValues(resolveMultipleTypes(_).head)
        .toMap //TODO this should be resolved in a private def in context of Seq monad (similarly to multipleTypes)
    } yield
      signature
        .modify(_.receiver)
        .setTo(receiver)
        .modify(_.arguments)
        .setTo(args)
        .modify(_.result)
        .setTo(result)
        .modify(_.context.constraints)
        .setTo(constraints)
  }

  private def resolvePossibleTypes(typ: Type): Seq[Type] = {
    val resolved = typ match {
      case t: TypeVariable =>
        Seq(
          t.modify(_.dri)
            .setTo(DRI(None, None, None, parsedDriPrefix + t.name.name).some)
        )
      case t: ConcreteType =>
        ancestryGraph.values.map(_._1).filter(_.name == t.name).toSeq
      case t: GenericType =>
        for {
          generic <- ancestryGraph.values.map(_._1).filter(_.name == t.name).toSeq
          params <- resolveMultipleTypes(t.params.map(_.typ))
            .map(_.zip(generic.params).map {
              case (p, v) => zipVariance(p, v)
            })
        } yield copyDRI(t.modify(_.params).setTo(params), generic.dri)
      case t => Seq(t)
    }
    resolved.filter(_.params.size == typ.params.size)
  }

  private def copyDRI(typ: Type, dri: Option[DRI]): Type = typ match {
    case t: GenericType  => t.modify(_.base).using(copyDRI(_, dri))
    case t: ConcreteType => t.modify(_.dri).setTo(dri)
    case _ => typ
  }

  private def resolveMultipleTypes(args: Seq[Type]): Seq[Seq[Type]] = {
    args match {
      case Nil => Seq(Seq.empty)
      case h :: t =>
        for {
          arg <- resolvePossibleTypes(h)
          rest <- resolveMultipleTypes(t)
        } yield arg +: rest
    }
  }
}
