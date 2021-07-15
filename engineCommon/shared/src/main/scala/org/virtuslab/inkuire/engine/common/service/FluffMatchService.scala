package org.virtuslab.inkuire.engine.common.service

import cats.data.{State, StateT}
import org.virtuslab.inkuire.engine.common.model._
import com.softwaremill.quicklens._
import cats.implicits._
import scala.util.Random

class FluffMatchService(val inkuireDb: InkuireDb) extends BaseMatchService with VarianceOps {

  val ancestryGraph: AncestryGraph = AncestryGraph(inkuireDb.types, inkuireDb.conversions)

  implicit class TypeOps(sgn: Signature) {
    def canSubstituteFor(supr: Signature): Boolean = {
      val ok = for {
        okTypes <- ancestryGraph.checkTypesWithVariances(
          sgn.typesWithVariances,
          supr.typesWithVariances,
          sgn.context |+| supr.context
        )

        bindings <- State.get[VariableBindings]
        okBindings = checkBindings(bindings)
      } yield okTypes && okBindings
      ok.runA(VariableBindings.empty).value
    }
  }

  override def |?|(resolveResult: ResolveResult)(against: ExternalSignature): Boolean = {
    resolveResult.signatures.exists(against.signature.canSubstituteFor(_))
  }

  override def |??|(resolveResult: ResolveResult): Seq[ExternalSignature] = {
    val actualSignatures = resolveResult.signatures.foldLeft(resolveResult.signatures) {
      case (acc, against) =>
        acc.filter { sgn =>
          sgn == against || !sgn.canSubstituteFor(against) // TODO this can possibly fail for unresolved variance
        }
    }
    inkuireDb.functions
      .filter(|?|(resolveResult.modify(_.signatures).setTo(actualSignatures)))
  }

  private def checkBindings(bindings: VariableBindings): Boolean = {
    bindings.bindings.values.forall { types =>
      types
        .sliding(2, 1)
        .forall {
          case (a: Type) :: (b: Type) :: Nil =>
            (ancestryGraph.getAllParentsITIDs(a).contains(b.itid.get) ||
              ancestryGraph.getAllParentsITIDs(b).contains(a.itid.get)) &&
              a.params.map(_.typ).zip(b.params.map(_.typ)).forall {
                case (a: Type, b: Type) => a.itid == b.itid
                case _ => false
              }
          case _ :: _ :: Nil => false
          case _ => true
        }
    } && !TypeVariablesGraph(bindings).hasCyclicDependency
  }
}

case class TypeVariablesGraph(variableBindings: VariableBindings) {
  val dependencyGraph: Map[ITID, Seq[ITID]] = variableBindings.bindings.view.mapValues {
    _.flatMap {
      case g: Type if g.params.nonEmpty => retrieveVariables(g)
      case _ => Seq()
    }.distinct
  }.toMap

  private def retrieveVariables(t: TypeLike): Seq[ITID] =
    t match {
      case t: Type if t.isVariable => Seq(t.itid.get)
      case g: Type                 => g.params.map(_.typ).flatMap(retrieveVariables)
      case _ => Seq()
    }

  def hasCyclicDependency: Boolean = {
    case class DfsState(visited: Set[ITID] = Set.empty, stack: Set[ITID] = Set.empty)

    def loop(current: ITID): State[DfsState, Boolean] =
      for {
        dfsState <- State.get[DfsState]
        cycle    = dfsState.stack.contains(current)
        visited  = dfsState.visited.contains(current)
        newState = dfsState.modifyAll(_.visited, _.stack).using(_ + current)
        _ <- State.set[DfsState](newState)
        f <-
          if (!visited)
            dependencyGraph
              .getOrElse(current, Seq())
              .toList
              .traverse(loop)
          else State.pure[DfsState, List[Boolean]](List())
        _ <- State.modify[DfsState](s => s.modify(_.stack).using(_ - current))
      } yield cycle || f.exists(identity)

    dependencyGraph.keys.toList
      .traverse { v =>
        for {
          dfsState <- State.get[DfsState]
          flag <- if (dfsState.visited.contains(v)) State.pure[DfsState, Boolean](false) else loop(v)
        } yield flag
      }
      .map(_.exists(identity))
      .runA(DfsState())
      .value
  }
}

case class AncestryGraph(nodes: Map[ITID, (Type, Seq[Type])], implicitConversions: Map[ITID, Seq[Type]])
  extends VarianceOps {

  implicit class TypeOps(typ: TypeLike) {
    def isSubTypeOf(supr: TypeLike)(context: SignatureContext): State[VariableBindings, Boolean] = {
      (typ, supr) match {
        case (t: Type, _) if t.isStarProjection => State.pure(true)
        case (_, s: Type) if s.isStarProjection => State.pure(true)
        case (AndType(left, right), supr) =>
          left.isSubTypeOf(supr)(context).flatMap { res =>
            if (res) right.isSubTypeOf(supr)(context)
            else State.pure(false)
          }
        case (typ, AndType(left, right)) =>
          typ.isSubTypeOf(left)(context).flatMap { res =>
            if (res) State.pure(true)
            else typ.isSubTypeOf(right)(context)
          }
        case (OrType(left, right), supr) =>
          left.isSubTypeOf(supr)(context).flatMap { res =>
            if (res) State.pure(true)
            else right.isSubTypeOf(supr)(context)
          }
        case (typ, OrType(left, right)) =>
          typ.isSubTypeOf(left)(context).flatMap { res =>
            if (res) typ.isSubTypeOf(right)(context)
            else State.pure(false)
          }
        case (typ: TypeLambda, supr: TypeLambda) =>
          val dummyTypes = genDummyTypes(typ.args.size)
          val typResult = substituteBindings(typ.result, typ.args.flatMap(_.itid).zip(dummyTypes).toMap)
          val suprResult = substituteBindings(supr.result, supr.args.flatMap(_.itid).zip(dummyTypes).toMap)
          if (typ.args.size == supr.args.size) typResult.isSubTypeOf(suprResult)(context)
          else State.pure(false)
        case (_: TypeLambda, _) =>
          State.pure(false)
        case (_, _: TypeLambda) =>
          State.pure(false)
        //TODO #58 Support for TypeVariables as GenericTypes or not
        case (typ: Type, supr: Type) if typ.isVariable && typ.isGeneric =>
          State.modify[VariableBindings](_.add(typ.itid.get, supr.modify(_.params).setTo(Seq.empty))) >>
            checkTypeParamsByVariance(typ, supr, context)
        //TODO #58 Support for TypeVariables as GenericTypes or not
        case (typ: Type, supr: Type) if supr.isVariable && supr.isGeneric =>
          State.modify[VariableBindings](_.add(supr.itid.get, typ.modify(_.params).setTo(Seq.empty))) >>
            checkTypeParamsByVariance(typ, supr, context)
        case (typ: Type, supr: Type) if typ.isVariable && supr.isVariable =>
          val typConstraints  = context.constraints.get(typ.name.name).toSeq.flatten
          val suprConstraints = context.constraints.get(supr.name.name).toSeq.flatten
          State.modify[VariableBindings](_.add(typ.itid.get, supr).add(supr.itid.get, typ)) >>
            State.pure(typConstraints == suprConstraints) // TODO #56 Better 'equality' between two TypeVariables
        case (typ: Type, supr: Type) if supr.isVariable =>
          if (supr.itid.get.isParsed) {
            State.modify[VariableBindings](_.add(supr.itid.get, typ)) >>
              State.pure(true)
          } else {
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
          }
        case (typ: Type, supr: Type) if typ.isVariable =>
          if (typ.itid.get.isParsed) {
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
          } else {
            State.modify[VariableBindings](_.add(typ.itid.get, supr)) >>
              State.pure(true)
          }
        case (typ: Type, supr: Type) if typ.itid == supr.itid => checkTypeParamsByVariance(typ, supr, context)
        case (typ: Type, supr: Type) =>
          if (nodes.contains(typ.itid.get)) {
            specializeParents(typ, nodes(typ.itid.get)).toList
              .foldLeft(State.pure[VariableBindings, Boolean](false)) {
                case (acc, t) =>
                  acc.flatMap { cond =>
                    if (cond) State.pure[VariableBindings, Boolean](true)
                    else t.isSubTypeOf(supr)(context)
                  }
              }
          } else State.pure(false) //TODO remove when everything is correctly resolved
      }
    }
  }

  private def specializeParents(concreteType: Type, node: (Type, Seq[Type])): Seq[TypeLike] = {
    val (declaration, parents) = node
    def resITID(t: TypeLike): Option[ITID] = t match {
      case t: Type => t.itid
      case t: TypeLambda => resITID(t.result)
      case _ => None
    }
    val bindings: Map[ITID, TypeLike] =
      declaration.params
        .map(_.typ).map(resITID)
        .flatMap(identity)
        .zip(concreteType.params.map(_.typ))
        .toMap
    parents.map(substituteBindings(_, bindings))
  }

  private def substituteBindings(parent: TypeLike, bindings: Map[ITID, TypeLike]): TypeLike = parent match {
    case t: Type if t.isVariable =>
      t.itid match {
        case None => t.modify(_.params.each.typ).using(substituteBindings(_, bindings))
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
        itid = Some(ITID(name, isParsed = false)),
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
        val typParam = v1.typ
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
