package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model._
import com.softwaremill.quicklens._
import cats.implicits._

class DefaultSignatureResolver(inkuireDb: InkuireDb)
  extends BaseSignatureResolver
  with VarianceOps {

  val ag = AncestryGraph(inkuireDb.types, inkuireDb.conversions, inkuireDb.typeAliases)
  val implicitConversions = inkuireDb.conversions
  val ancestryGraph = inkuireDb.types

  override def resolve(parsed: Signature): Either[String, ResolveResult] = {
    val signatures = resolveAllPossibleSignatures(parsed).toList
      .map(moveToReceiverIfPossible)
      .flatMap { sgn => convertReceivers(sgn).toList }
      .flatMap { sgn => permutateParams(sgn).toList }
      .distinct
    signatures match {
      //TODO change to sth more informative, actual unresolved types
      case List() => Left(resolveError("Could not resolve types in provided signature"))
      case _      => Right(ResolveResult(signatures))
    }
  }

  def resolveError(msg: String): String = s"Resolving error: $msg"

  private def moveToReceiverIfPossible(signature: Signature): Signature = {
    if (signature.receiver.nonEmpty) signature
    else if (signature.arguments.isEmpty) signature
    else
      signature
        .modify(_.receiver)
        .setTo(Some(signature.arguments.head))
        .modify(_.arguments)
        .using(_.drop(1))
  }

  private def convertReceivers(signature: Signature): Seq[Signature] = {
    if (signature.receiver.isEmpty) List(signature)
    else {
      signature.receiver.toSeq
        .flatMap { rcvrVar =>
          rcvrVar.typ match {
            case t: Type =>
              t.itid.toSeq.flatMap { rcvrITID =>
                implicitConversions.get(rcvrITID).toSeq.flatten
              }
            case t => Seq(t)
          }
        }
        .map { rcvrType =>
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

  private def mostGeneral(types: Seq[TypeLike]): Seq[TypeLike] = { //TODO can be applied deeper if needed
    types.filter {
      case typ: Type =>
        (ag.getAllParentsITIDs(typ).toSet - typ.itid.get)
          .intersect(types.collect { case t: Type => t.itid.get }.toSet)
          .isEmpty
      case _ => true
    }.distinct
  }

  private def mostSpecific(types: Seq[TypeLike]): Seq[TypeLike] = { //TODO can be applied deeper if needed
    types
      .foldLeft(types) {
        case (acc, typ: Type) =>
          acc.filter {
            case t: Type =>
              !(ag.getAllParentsITIDs(typ).toSet - typ.itid.get).contains(t.itid.get)
            case _ => true
          }
        case (acc, _) => acc
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

  private def resolvePossibleTypes(typ: TypeLike): Seq[TypeLike] = {
    val resolved = typ match {
      case t: Type if t.isStarProjection => Seq(t)
      case t: Type if t.isVariable && !t.isGeneric =>
        Seq(
          t.modify(_.itid)
            .setTo(ITID(t.name.name, isParsed = true).some)
        )
      case t: Type if t.isVariable && t.isGeneric => //TODO I'm fairly certain that this case works for Arrow.kt - Kind
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
      case t: Type if t.isGeneric =>
        for {
          generic <- ancestryGraph.values.map(_._1).filter(_.name == t.name).toSeq
          params <- resolveMultipleTypes(t.params.map(_.typ))
            .map(_.zip(generic.params).map {
              case (p, v) => p.zipVariance(v)
            })
        } yield copyITID(t.modify(_.params).setTo(params), generic.itid)
      case t: Type =>
        ancestryGraph.values.map(_._1).filter(_.name == t.name).toSeq
      case t =>
        Seq(t)
    }
    resolved
      .map(_ -> typ)
      .filter {
        case (t: Type, typ: Type) => t.params.size == typ.params.size
        case _ => true
      }
      .map(_._1)
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

  private def resolveMultipleTypes(args: Seq[TypeLike]): Seq[Seq[TypeLike]] = {
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
