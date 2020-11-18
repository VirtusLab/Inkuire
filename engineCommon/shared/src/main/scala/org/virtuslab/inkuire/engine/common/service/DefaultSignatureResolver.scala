package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model._
import com.softwaremill.quicklens._
import cats.implicits._

class DefaultSignatureResolver(ancestryGraph: Map[ITID, (Type, Seq[Type])])
  extends BaseSignatureResolver
  with VarianceOps {

  override def resolve(parsed: Signature): ResolveResult =
    ResolveResult {
      resolveAllPossibleSignatures(parsed).toList >>= { sgn =>
        permutateParams(sgn).toList
      }
    }

  private def permutateParams(signature: Signature): Seq[Signature] = {
    (signature.receiver ++ signature.arguments).toList.permutations
      .map { params =>
        if (signature.receiver.nonEmpty) {
          signature
            .modify(_.receiver)
            .setTo(params.headOption)
            .modify(_.arguments)
            .setTo(params.drop(1))
        } else {
          signature
            .modify(_.arguments)
            .setTo(params)
        }
      }
      .distinct
      .toSeq
  }

  private def resolveAllPossibleSignatures(signature: Signature): Seq[Signature] = {
    for {
      receiver <-
        signature.receiver
          .fold[Seq[Option[Type]]](Seq(None))(r => resolvePossibleTypes(r.typ).map(_.some))
          .map(_.map(Contravariance))
      args <- resolveMultipleTypes(signature.arguments.map(_.typ)).map(_.map(Contravariance))
      result <- resolvePossibleTypes(signature.result.typ).map(Covariance)
      constraints =
        signature.context.constraints.view
          .mapValues(resolveMultipleTypes(_).head)
          .toMap //TODO this should be resolved in a private def in context of Seq monad (similarly to multipleTypes)
    } yield signature
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
          t.modify(_.itid)
            .setTo(ITID(t.name.name, isParsed = true).some)
        )
      case t: ConcreteType =>
        ancestryGraph.values.map(_._1).filter(_.name == t.name).toSeq
      case t: GenericType if t.isVariable =>
        for {
          kind <-
            ancestryGraph.values
              .map(_._1)
              .filter(_.name == t.name)
              .filter(_.params.size == t.params.size - 1)
              .filter(_.isInstanceOf[GenericType])
              .toSeq
          params <- resolveMultipleTypes(t.params.map(_.typ))
        } yield kind.asInstanceOf[GenericType].modify(_.params).setTo((t.base +: params).map(Invariance.apply))
      case t: GenericType =>
        for {
          generic <- ancestryGraph.values.map(_._1).filter(_.name == t.name).toSeq
          params <- resolveMultipleTypes(t.params.map(_.typ))
            .map(_.zip(generic.params).map {
              case (p, v) => zipVariance(p, v)
            })
        } yield copyITID(t.modify(_.params).setTo(params), generic.itid)
      case t => Seq(t)
    }
    resolved.filter(_.params.size == typ.params.size)
  }

  private def copyITID(typ: Type, dri: Option[ITID]): Type =
    typ match {
      case t: GenericType  => t.modify(_.base).using(copyITID(_, dri))
      case t: ConcreteType => t.modify(_.itid).setTo(dri)
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
