package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model._
import com.softwaremill.quicklens._
import cats.implicits._
import cats.Contravariant

class DefaultSignatureResolver(ancestryGraph: Map[ITID, (Type, Seq[Type])], implicitConversions: Map[ITID, Seq[Type]])
  extends BaseSignatureResolver
  with VarianceOps {

  val ag = AncestryGraph(ancestryGraph, implicitConversions)

  override def resolve(parsed: Signature): ResolveResult =
    ResolveResult {
      resolveAllPossibleSignatures(parsed).toList
        .map(moveToReceiverIfPossible)
        .flatMap { sgn => convertReceivers(sgn).toList }
        .flatMap { sgn => permutateParams(sgn).toList }.distinct
    }

  private def moveToReceiverIfPossible(signature: Signature): Signature = {
    if (signature.receiver.nonEmpty) signature
    else if (signature.arguments.isEmpty) signature
    else
      signature
        .modify(_.receiver).setTo(Some(signature.arguments.head))
        .modify(_.arguments).using(_.drop(1))
  }

  private def convertReceivers(signature: Signature): Seq[Signature] = {
    if (signature.receiver.isEmpty) List(signature)
    else {
      signature.receiver.toSeq.flatMap { rcvrVar =>
        rcvrVar.typ.itid.toSeq.flatMap { rcvrITID =>
          if (rcvrITID.uuid.contains("String")) {
            println(rcvrITID)
            println(implicitConversions.get(rcvrITID))
          }
          implicitConversions.get(rcvrITID).toSeq.flatten
        }
      }.map { rcvrType =>
        signature.modify(_.receiver.each.typ).setTo(rcvrType)
      } :+ signature
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

  private def mostGeneral(types: Seq[Type]): Seq[Type] = { //TODO can be applied deeper if needed
    types.filter { typ =>
      (ag.getAllParentsITIDs(typ).toSet - typ.itid.get).intersect(types.map(_.itid.get).toSet).isEmpty
    }.distinct
  }

  private def mostSpecific(types: Seq[Type]): Seq[Type] = { //TODO can be applied deeper if needed
    types
      .foldLeft(types) {
        case (acc, typ) =>
          acc.filter { t =>
            !(ag.getAllParentsITIDs(typ).toSet - typ.itid.get).contains(t.itid.get)
          }
      }
      .distinct
  }

  private def resolveAllPossibleSignatures(signature: Signature): Seq[Signature] = {
    for {
      receiver <-
        signature.receiver
          .fold[Seq[Option[Contravariance]]](Seq(None))(r =>
            resolvePossibleVariances(Contravariance(r.typ)).map(_.some.asInstanceOf[Option[Contravariance]])
          )
      args <- resolveMultipleVariances[Contravariance](signature.arguments.map(_.typ).map(Contravariance))
      result <- resolvePossibleVariances(Covariance(signature.result.typ))
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

  private def resolvePossibleVariances[V <: Variance](v: V): Seq[V] = {
    val typ   = v.typ
    val types = resolvePossibleTypes(typ)
    if (v.isInstanceOf[Contravariance]) {
      mostSpecific(types).map(_.zipVariance(v).asInstanceOf[V])
    } else if (v.isInstanceOf[Covariance]) {
      mostGeneral(types).map(_.zipVariance(v).asInstanceOf[V])
    } else types.map(_.zipVariance(v).asInstanceOf[V])
  }

  private def resolvePossibleTypes(typ: Type): Seq[Type] = {
    val resolved = typ match {
      case t if t.isStarProjection => Seq(t)
      case t if t.isVariable && !t.isGeneric =>
        Seq(
          t.modify(_.itid)
            .setTo(ITID(t.name.name, isParsed = true).some)
        )
      case t if t.isVariable && t.isGeneric =>
        for {
          kind <-
            ancestryGraph.values
              .map(_._1)
              .filter(_.name == t.name)
              .filter(_.params.size == t.params.size - 1)
              .filter(_.params.nonEmpty)
              .toSeq
          params <- resolveMultipleTypes(t.params.map(_.typ))
        } yield kind.modify(_.params).setTo((t +: params).map(Invariance.apply))
      case t if t.isGeneric =>
        for {
          generic <- ancestryGraph.values.map(_._1).filter(_.name == t.name).toSeq
          params <- resolveMultipleTypes(t.params.map(_.typ))
            .map(_.zip(generic.params).map {
              case (p, v) => p.zipVariance(v)
            })
        } yield copyITID(t.modify(_.params).setTo(params), generic.itid)
      case t =>
        ancestryGraph.values.map(_._1).filter(_.name == t.name).toSeq
    }
    resolved.filter(_.params.size == typ.params.size)
  }

  private def copyITID(typ: Type, dri: Option[ITID]): Type =
    typ match {
      case t if !t.isVariable => t.modify(_.itid).setTo(dri)
      case _                  => typ
    }

  private def resolveMultipleVariances[V <: Variance](args: Seq[V]): Seq[Seq[V]] = {
    args match {
      case Nil => Seq(Seq.empty)
      case h :: t =>
        for {
          arg <- resolvePossibleVariances[V](h)
          rest <- resolveMultipleVariances[V](t)
        } yield arg +: rest
    }
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
