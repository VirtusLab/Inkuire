package org.virtuslab.inkuire.engine.service

import com.softwaremill.quicklens._
import org.virtuslab.inkuire.engine.api._
import org.virtuslab.inkuire.engine.model._

import scala.util.chaining._

class DefaultSignatureResolver(inkuireDb: InkuireDb) extends BaseSignatureResolver with MatchingOps {

  val ag: AncestryGraph = AncestryGraph(inkuireDb.types, inkuireDb.implicitConversions, inkuireDb.typeAliases)
  val implicitConversions = inkuireDb.implicitConversions
  val ancestryGraph       = inkuireDb.types

  override def resolve(parsed: ParsedSignature): Either[String, ResolveResult] = {
    val signatures = resolveAllPossibleSignatures(parsed.signature).map(
      _.toList
        .map(moveToReceiverIfPossible)
        .distinct
    )
    signatures match {
      case Left(unresolvedType) => Left(resolveError(s"Could not resolve type: $unresolvedType"))
      case Right(signatures)    => Right(ResolveResult(signatures, parsed.filters))
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

  private def resolveAllPossibleSignatures(signature: Signature): Either[String, Seq[Signature]] = {
    for {
      receiver <-
        signature.receiver
          .fold[Either[String, Seq[Option[Contravariance]]]](Right(Seq(None)))(r =>
            resolvePossibleVariances(Contravariance(r.typ)).map(_.map(_.pipe(Some.apply)))
          )
      args <- resolveMultipleVariances[Contravariance](signature.arguments.map(_.typ).map(Contravariance))
      result <- resolvePossibleVariances(Covariance(signature.result.typ))
    } yield {
      for {
        receiver <- receiver
        args <- args
        result <- result
        constraints =
          signature.context.constraints.view
            .mapValues(resolveMultipleTypes(_).toOption.get.head)
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
  }

  private def resolvePossibleVariances[V <: Variance](v: V): Either[String, Seq[V]] = {
    val typ   = v.typ
    val types = resolvePossibleTypes(typ)
    if (v.isInstanceOf[Contravariance]) {
      types.map(types => mostSpecific(types).map(_.zipVariance(v).asInstanceOf[V]))
    } else if (v.isInstanceOf[Covariance]) {
      types.map(types => mostGeneral(types).map(_.zipVariance(v).asInstanceOf[V]))
    } else types.map(_.map(_.zipVariance(v).asInstanceOf[V]))
  }

  private def resolvePossibleTypes(typ: TypeLike): Either[String, Seq[TypeLike]] = {
    val resolved: Either[String, Seq[TypeLike]] = typ match {
      case t: Type if t.isStarProjection => Right(Seq(t))
      case t: Type if t.isVariable =>
        resolveMultipleTypes(t.params.map(_.typ)).map(_.map { params =>
          t.modify(_.itid)
            .setTo(ITID(t.name.name, isParsed = true).pipe(Some.apply))
            .modify(_.params)
            .setTo(params.zipVariances(t.params))
        })
      case t: Type if t.isGeneric =>
        resolveMultipleTypes(t.params.map(_.typ)).flatMap { params =>
          ancestryGraph.values.map(_._1).filter(_.name == t.name).toSeq match {
            case Nil => Left(t.name.name)
            case _ =>
              Right(for {
                generic <- ancestryGraph.values.map(_._1).filter(_.name == t.name).toSeq
                params <- params.map(_.zipVariances(generic.params))
              } yield copyITID(t.modify(_.params).setTo(params), generic.itid))
          }
        }
      case t: Type =>
        ancestryGraph.values.map(_._1).filter(_.name == t.name).toSeq match {
          case Nil   => Left(t.name.name)
          case types => Right(types)
        }
      case OrType(left, right) =>
        for {
          l <- resolvePossibleTypes(left)
          r <- resolvePossibleTypes(right)
        } yield {
          for {
            r <- r
            l <- l
          } yield OrType(l, r)
        }
      case AndType(left, right) =>
        for {
          l <- resolvePossibleTypes(left)
          r <- resolvePossibleTypes(right)
        } yield {
          for {
            r <- r
            l <- l
          } yield AndType(l, r)
        }
      case t =>
        Right(Seq(t))
    }
    resolved
      .map(
        _.map(_ -> typ)
          .filter {
            case (t: Type, typ: Type) => t.params.size == typ.params.size
            case _ => true
          }
          .map(_._1)
      )
  }

  private def copyITID(typ: Type, dri: Option[ITID]): Type =
    typ match {
      case t if !t.isVariable => t.modify(_.itid).setTo(dri)
      case _                  => typ
    }

  private def resolveMultipleVariances[V <: Variance](args: Seq[V]): Either[String, Seq[Seq[V]]] = {
    args match {
      case Nil => Right(Seq(Seq.empty))
      case h :: t =>
        for {
          arg <- resolvePossibleVariances[V](h)
          rest <- resolveMultipleVariances[V](t)
        } yield {
          for {
            arg <- arg
            rest <- rest
          } yield arg +: rest
        }
    }
  }

  private def resolveMultipleTypes(args: Seq[TypeLike]): Either[String, Seq[Seq[TypeLike]]] = {
    args match {
      case Nil => Right(Seq(Seq.empty))
      case h :: t =>
        for {
          arg <- resolvePossibleTypes(h)
          rest <- resolveMultipleTypes(t)
        } yield {
          for {
            arg <- arg
            rest <- rest
          } yield arg +: rest
        }
    }
  }
}
