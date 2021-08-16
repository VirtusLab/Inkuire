package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model._
import cats.data.State
import cats.implicits._
import com.softwaremill.quicklens._
import scala.util.Random
import scala.util.chaining._
import scala.collection.mutable.{Set => MSet}

case class AncestryGraph(
  nodes:               Map[ITID, (Type, Seq[Type])],
  implicitConversions: Map[TypeLike, Seq[Type]],
  typeAliases:         Map[ITID, TypeLike]
) extends VarianceOps {

  val cacheNeg: MSet[(TypeLike, TypeLike)] = MSet.empty

  implicit class TypeOps(typ: TypeLike) {
    def isSubTypeOf(supr: TypeLike)(context: SignatureContext): State[VariableBindings, Boolean] = {
      if (cacheNeg.contains((typ, supr))) State.pure(false)
      else {
        val res: State[VariableBindings, Boolean] = (typ, supr) match {
          case (t: Type, _) if t.isStarProjection => State.pure(true)
          case (_, s: Type) if s.isStarProjection => State.pure(true)
          case (AndType(left, right), supr) =>
            left.isSubTypeOf(supr)(context).flatMap { res =>
              if (res) State.pure(true)
              else right.isSubTypeOf(supr)(context)
            }
          case (typ, AndType(left, right)) =>
            typ.isSubTypeOf(left)(context).flatMap { res =>
              if (res) typ.isSubTypeOf(right)(context)
              else State.pure(false)
            }
          case (OrType(left, right), supr) =>
            left.isSubTypeOf(supr)(context).flatMap { res =>
              if (res) right.isSubTypeOf(supr)(context)
              else State.pure(false)
            }
          case (typ, OrType(left, right)) =>
            typ.isSubTypeOf(left)(context).flatMap { res =>
              if (res) State.pure(true)
              else typ.isSubTypeOf(right)(context)
            }
          case (typ: TypeLambda, supr: TypeLambda) =>
            val dummyTypes = genDummyTypes(typ.args.size)
            val typResult  = substituteBindings(typ.result, typ.args.flatMap(_.itid).zip(dummyTypes).toMap)
            val suprResult = substituteBindings(supr.result, supr.args.flatMap(_.itid).zip(dummyTypes).toMap)
            if (typ.args.size == supr.args.size) typResult.isSubTypeOf(suprResult)(context)
            else State.pure(false)
          case (_: TypeLambda, _) =>
            State.pure(false)
          case (_, _: TypeLambda) =>
            State.pure(false)
          case (typ: Type, supr: Type) if typ.isVariable && typ.isGeneric =>
            State.modify[VariableBindings](_.add(typ.itid.get, supr.modify(_.params).setTo(Seq.empty))) >>
              checkTypeParamsByVariance(typ, supr, context)
          case (typ: Type, supr: Type) if supr.isVariable && supr.isGeneric =>
            State.modify[VariableBindings](_.add(supr.itid.get, typ.modify(_.params).setTo(Seq.empty))) >>
              checkTypeParamsByVariance(typ, supr, context)
          case (typ: Type, supr: Type) if typ.isVariable && supr.isVariable =>
            val typConstraints  = context.constraints.get(typ.name.name).toSeq.flatten
            val suprConstraints = context.constraints.get(supr.name.name).toSeq.flatten
            State.modify[VariableBindings](_.add(typ.itid.get, supr).add(supr.itid.get, typ)) >>
              State.pure(typConstraints == suprConstraints) // TODO #56 Better 'equality' between two TypeVariables
          case (typ: Type, supr: Type) if supr.isVariable =>
            val constraints = context.constraints.get(supr.name.name).toSeq.flatten.toList
            State.modify[VariableBindings](_.add(supr.itid.get, typ)) >>
              constraints
                .foldLeft(State.pure[VariableBindings, Boolean](true)) {
                  case (acc, t) =>
                    acc.flatMap { cond =>
                      if (cond) typ.isSubTypeOf(t)(context)
                      else State.pure(false)
                    }
                }
          case (typ: Type, supr: Type) if typ.isVariable =>
            val constraints = context.constraints.get(typ.name.name).toSeq.flatten.toList
            State.modify[VariableBindings](_.add(typ.itid.get, supr)) >> {
              if (constraints.nonEmpty) {
                constraints
                  .foldLeft(State.pure[VariableBindings, Boolean](false)) {
                    case (acc, t) =>
                      acc.flatMap { cond =>
                        if (cond) State.pure[VariableBindings, Boolean](true)
                        else t.isSubTypeOf(supr)(context)
                      }
                  }
              } else State.pure(true)
            }
          case (typ: Type, supr: Type) if typ.itid == supr.itid => checkTypeParamsByVariance(typ, supr, context)
          case (typ: Type, supr: Type) =>
            nodes
              .get(typ.itid.get)
              .toList
              .flatMap(node => specializeParents(typ, node))
              .map(_ -> supr)
              .++(typeAliases.get(typ.itid.get).toList.flatMap(alias => dealias(typ, alias)).map(_ -> supr))
              .++(typeAliases.get(supr.itid.get).toList.flatMap(alias => dealias(supr, alias)).map(typ -> _))
              .foldLeft(State.pure[VariableBindings, Boolean](false)) {
                case (acc, (t, s)) =>
                  acc.flatMap { cond =>
                    if (cond) State.pure(true)
                    else t.isSubTypeOf(s)(context)
                  }
              }
        }
        res
          .map { b =>
            if (!b) cacheNeg.add((typ, supr))
            b
          }
      }
    }
  }

  def dealias(concreteType: Type, node: TypeLike): Option[TypeLike] = (concreteType, node) match {
    case (t: Type, rhs: Type) if !t.isGeneric =>
      Some(rhs)
    case (t: Type, rhs: TypeLambda) if t.params.size == rhs.args.size =>
      Some(substituteBindings(rhs.result, rhs.args.flatMap(_.itid).zip(t.params.map(_.typ)).toMap))
    case _ =>
      None
  }

  private def specializeParents(concreteType: Type, node: (Type, Seq[TypeLike])): Seq[TypeLike] = {
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

  private def substituteBindings(parent: TypeLike, bindings: Map[ITID, TypeLike]): TypeLike = parent match {
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

  private def genDummyTypes(n: Int) =
    1.to(n).map { i =>
      val name = s"dummy$i${Random.nextString(10)}"
      Type(
        name = TypeName(name),
        itid = Some(ITID(name)),
        isVariable = true
      )
    }

  def getAllParentsITIDs(tpe: Type): Seq[ITID] = {
    tpe.itid.get +: nodes.get(tpe.itid.get).toSeq.flatMap(_._2).flatMap(getAllParentsITIDs(_))
  }

  def checkTypesWithVariances(
    types:   Seq[Variance],
    suprs:   Seq[Variance],
    context: SignatureContext
  ): State[VariableBindings, Boolean] = {
    if (types.size == suprs.size) {
      types
        .zip(suprs)
        .toList
        .foldLeft(State.pure[VariableBindings, Boolean](true)) {
          case (acc, (externalType, queryType)) =>
            acc.flatMap { cond =>
              if (cond) checkByVariance(externalType, queryType, context)
              else State.pure[VariableBindings, Boolean](false)
            }
        }
    } else State.pure(false)
  }

  private def checkTypeParamsByVariance(
    typ:     Type,
    supr:    Type,
    context: SignatureContext
  ): State[VariableBindings, Boolean] = {
    val typVariance  = writeVariancesFromDRI(typ)
    val suprVariance = writeVariancesFromDRI(supr)
    checkTypesWithVariances(typVariance.params, suprVariance.params, context)
  }

  def checkByVariance(
    typ:     Variance,
    supr:    Variance,
    context: SignatureContext
  ): State[VariableBindings, Boolean] = {
    (typ, supr) match {
      case (Covariance(typParam), Covariance(suprParam)) =>
        typParam.isSubTypeOf(suprParam)(context)
      case (Contravariance(typParam), Contravariance(suprParam)) =>
        suprParam.isSubTypeOf(typParam)(context)
      case (Invariance(typParam), Invariance(suprParam)) =>
        typParam.isSubTypeOf(suprParam)(context) >>= { res1 =>
          if (res1) suprParam.isSubTypeOf(typParam)(context)
          else State.pure(false)
        }
      case (v1, v2) => // Treating not matching variances as invariant
        val typParam  = v1.typ
        val suprParam = v2.typ
        typParam.isSubTypeOf(suprParam)(context) >>= { res1 =>
          if (res1) suprParam.isSubTypeOf(typParam)(context)
          else State.pure(false)
        }
    }
  }

  private def writeVariancesFromDRI: Type => Type = {
    case typ if typ.params.nonEmpty =>
      typ
        .modify(_.params)
        .using { params =>
          if (typ.itid.nonEmpty && nodes.contains(typ.itid.get)) {
            params.zip(nodes(typ.itid.get)._1.params).map {
              case (t, v) => t.typ.zipVariance(v)
            }
          } else params
        }
    case typ => typ
  }
}
