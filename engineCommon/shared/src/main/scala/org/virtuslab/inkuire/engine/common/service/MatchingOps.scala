package org.virtuslab.inkuire.engine.common.service

import com.softwaremill.quicklens._
import org.virtuslab.inkuire.engine.common.model._

import scala.util.Random

trait MatchingOps {

  protected def genDummyTypes(n: Int): IndexedSeq[Type] =
    1.to(n).map { i =>
      val name = s"dummy$i${Random.nextString(10)}"
      Type(
        name = TypeName(name),
        itid = Some(ITID(name, isParsed = false)),
        isVariable = true
      )
    }

  def substituteBindings(parent: TypeLike, bindings: Map[ITID, TypeLike]): TypeLike = parent match {
    case t: Type if t.isVariable =>
      t.itid match {
        case None       => t.modify(_.params.each.typ).using(substituteBindings(_, bindings))
        case Some(itid) => bindings.get(itid).getOrElse(t)
      }
    case t: Type =>
      t.modify(_.params.each.typ).using(substituteBindings(_, bindings))
    case t: OrType =>
      t.modifyAll(_.left, _.right).using(substituteBindings(_, bindings))
    case t: AndType =>
      t.modifyAll(_.left, _.right).using(substituteBindings(_, bindings))
    case t: TypeLambda =>
      t.modify(_.result).using(substituteBindings(_, bindings))
  }

  def dealias(concreteType: Type, node: TypeLike): Option[TypeLike] = (concreteType, node) match {
    case (t: Type, rhs: Type) if !t.isGeneric =>
      Some(rhs)
    case (t: Type, rhs: TypeLambda) if t.params.size == rhs.args.size =>
      Some(substituteBindings(rhs.result, rhs.args.flatMap(_.itid).zip(t.params.map(_.typ)).toMap))
    case _ =>
      None
  }

  def specializeParents(concreteType: Type, node: (Type, Seq[TypeLike])): Seq[TypeLike] = {
    val (declaration, parents) = node
    def resITID(t: TypeLike): Option[ITID] = t match {
      case t: Type       => t.itid
      case t: TypeLambda => resITID(t.result)
      case _ => None
    }
    val bindings: Map[ITID, TypeLike] =
      declaration.params
        .map(_.typ)
        .map(resITID)
        .flatMap(identity)
        .zip(concreteType.params.map(_.typ))
        .toMap
    parents.map(substituteBindings(_, bindings))
  }

  implicit class TypeMatchingOps(typ: TypeLike) {
    def zipVariance(v: Variance): Variance = v match {
      case _: Contravariance     => Contravariance(typ)
      case _: Covariance         => Covariance(typ)
      case _: Invariance         => Invariance(typ)
      case _: UnresolvedVariance => UnresolvedVariance(typ)
    }
  }

  implicit class TypeVariancesOps(types: Seq[TypeLike]) {
    def zipVariances(variances: Seq[Variance]): Seq[Variance] = types.zip(variances).map {
      case (t, v) => t.zipVariance(v)
    }
  }
}
