package org.virtuslab.inkuire.engine.common.service

import cats.data.State
import org.virtuslab.inkuire.engine.common.model._
import com.softwaremill.quicklens._
import cats.implicits._

class FluffMatchService(val inkuireDb: InkuireDb) extends BaseMatchService with VarianceOps {

  val ancestryGraph: AncestryGraph = AncestryGraph(inkuireDb.types)

  override def |?|(resolveResult: ResolveResult)(against: ExternalSignature): Boolean =
    resolveResult.signatures.exists { sgn =>
      val ok = for {
        okTypes <- ancestryGraph.checkTypesWithVariances(
          against.signature.typesWithVariances,
          sgn.typesWithVariances,
          against.signature.context |+| sgn.context
        )

        bindings <- State.get[VariableBindings]
        okBindings = checkBindings(bindings)
      } yield okTypes && okBindings
      ok.runA(VariableBindings.empty).value
    }

  override def |??|(resolveResult: ResolveResult): Seq[ExternalSignature] =
    inkuireDb.functions.filter(|?|(resolveResult))

  private def checkBindings(bindings: VariableBindings): Boolean = {
    bindings.bindings.values.forall { types =>
      types
        .sliding(2, 1)
        .forall {
          case a :: b :: Nil => a.itid == b.itid
          case _             => true
        }
    } && !TypeVariablesGraph(bindings).hasCyclicDependency
  }
}

case class TypeVariablesGraph(variableBindings: VariableBindings) {
  val dependencyGraph: Map[ITID, Seq[ITID]] = variableBindings.bindings.view
    .mapValues(_.flatMap {
      case g: GenericType => retrieveVariables(g)
      case _ => Seq()
    }.distinct)
    .toMap

  private def retrieveVariables(t: Type): Seq[ITID] =
    t match {
      case g: GenericType  => g.params.map(_.typ).flatMap(retrieveVariables)
      case t: TypeVariable => Seq(t.itid.get)
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

case class AncestryGraph(nodes: Map[ITID, (Type, Seq[Type])]) extends VarianceOps {

  def isSubType(
    typ:     Type,
    supr:    Type,
    context: SignatureContext
  ): State[VariableBindings, Boolean] = {
    (typ, supr) match {
      case (StarProjection, _) => State.pure(true)
      case (_, StarProjection) => State.pure(true)
      case (typ: TypeVariable, supr: TypeVariable) =>
        val typConstraints  = context.constraints.get(typ.name.name).toSeq.flatten
        val suprConstraints = context.constraints.get(supr.name.name).toSeq.flatten
        State.modify[VariableBindings](_.add(typ.itid.get, supr).add(supr.itid.get, typ)) >>
          State.pure(typConstraints == suprConstraints) // TODO #56 Better 'equality' between two TypeVariables
      case (typ, supr: TypeVariable) =>
        if (supr.itid.get.isParsed) {
          State.pure(false)
        } else {
          val constraints = context.constraints.get(supr.name.name).toSeq.flatten.toList
          State.modify[VariableBindings](_.add(supr.itid.get, typ)) >>
            constraints
              .traverse { t =>
                isSubType(typ, t, context)
              }
              .map { checks =>
                checks.forall(identity)
              }
        }
      case (typ: TypeVariable, supr) =>
        if (typ.itid.get.isParsed) {
          val constraints = context.constraints.get(typ.name.name).toSeq.flatten.toList
          State.modify[VariableBindings](_.add(typ.itid.get, supr)) >>
            constraints
              .traverse { t =>
                isSubType(t, supr, context)
              }
              .map { checks =>
                constraints.isEmpty || checks.exists(identity)
              }
        } else {
          State.pure(true)
        }
      case (typ: GenericType, _) if typ.isVariable =>
        State.pure(true) //TODO #58 Support for TypeVariables as GenericTypes or not
      case (_, supr: GenericType) if supr.isVariable =>
        State.pure(true) //TODO #58 Support for TypeVariables as GenericTypes or not
      case (typ, supr) if typ.itid == supr.itid => checkTypeParamsByVariance(typ, supr, context)
      case (typ, supr) =>
        if (nodes.contains(typ.itid.get))
          specializeParents(typ, nodes(typ.itid.get)).toList
            .traverse(isSubType(_, supr, context))
            .map(_.exists(identity))
        else State.pure(false) //TODO remove when everything is correctly resolved
    }
  }

  private def specializeParents(concreteType: Type, node: (Type, Seq[Type])): Seq[Type] = {
    val bindings = node._1.params.map(_.typ.name).zip(concreteType.params.map(_.typ)).toMap
    node._2.map {
      case t: GenericType if !t.base.isVariable =>
        t.modify(_.params.each.typ).using(p => bindings.getOrElse(p.name, p))
      case t => t
    }
  }

  private def specializeType(parsedType: Type, possibleMatch: Type): Type =
    (parsedType, possibleMatch) match {
      case (pT: GenericType, t: GenericType) =>
        t.modify(_.params)
          .using(_.zip(pT.params).map(p => specializeVariance(p._1, p._2)))
      case _ => possibleMatch
    }

  private def specializeVariance(parsedType: Variance, possibleMatch: Variance): Variance =
    (parsedType.typ, possibleMatch.typ) match {
      case (pT: GenericType, t: GenericType) =>
        val typ = t
          .modify(_.params)
          .using(_.zip(pT.params).map(p => specializeVariance(p._1, p._2)))
        zipVariance(typ, possibleMatch)
      case _ => possibleMatch
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
        .traverse {
          case (externalType, queryType) => checkByVariance(externalType, queryType, context)
        }
        .map(_.forall(identity))
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
      case (typ, supr) if typ.typ == StarProjection || supr.typ == StarProjection =>
        State.pure[VariableBindings, Boolean](true)
      case (Covariance(typParam), Covariance(suprParam)) => isSubType(typParam, suprParam, context)
      case (Contravariance(typParam), Contravariance(suprParam)) =>
        isSubType(suprParam, typParam, context)
      case (Invariance(typParam), Invariance(suprParam)) =>
        isSubType(typParam, suprParam, context) >>= { res1 =>
          isSubType(suprParam, typParam, context).fmap { res2 =>
            res1 && res2
          }
        }
    }
  }

  private def writeVariancesFromDRI: Type => Type = {
    case typ: GenericType =>
      typ
        .modify(_.params)
        .using { params =>
          if (typ.itid.nonEmpty && nodes.contains(typ.itid.get)) {
            params.zip(nodes(typ.itid.get)._1.params).map {
              case (t, v) => zipVariance(t.typ, v)
            }
          } else params
        }
    case typ => typ
  }
}

trait VarianceOps {
  def zipVariance(typ: Type, v: Variance): Variance = {
    v match {
      case _: Contravariance => Contravariance(typ)
      case _: Covariance     => Covariance(typ)
      case _: Invariance     => Invariance(typ)
    }
  }
}

case class VariableBindings(bindings: Map[ITID, Seq[Type]]) {
  def add(dri: ITID, typ: Type): VariableBindings = {
    VariableBindings {
      val types = bindings.getOrElse(dri, Seq.empty)
      bindings.updated(dri, types :+ typ)
    }
  }
}

object VariableBindings {
  def empty: VariableBindings =
    VariableBindings(Map.empty)
}
