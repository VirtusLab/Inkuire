package org.virtuslab.inkuire.engine.service

import cats.data.State
import org.virtuslab.inkuire.engine.model._
import com.softwaremill.quicklens._
import cats.implicits._

//TODO add support for star projection
//TODO handle case where one variable depends on the other like e.g. <A, B : List<A>> A.() -> B
class FluffMatchService(val inkuireDb: InkuireDb) extends BaseMatchService with FluffServiceOps {

  val parsedDriPrefix = "iri-" // IRI stands for Inkuire Resource Identifier

  val ancestryGraph: AncestryGraph = AncestryGraph(inkuireDb.types)

  override def |??|(signature: Signature): Seq[ExternalSignature] = {
    val signatures = resolveAllPossibleSignatures(signature)
    inkuireDb.functions.filter { eSgn =>
      signatures.exists { sgn =>
        val ok = for {
          okTypes <- ancestryGraph.checkTypesWithVariances(
            eSgn.signature.typesWithVariances,
            sgn.typesWithVariances,
            eSgn.signature.context |+| sgn.context
          )
          bindings <- State.get[VariableBindings]
          okBindings = checkBindings(bindings)
        } yield okTypes && okBindings
        ok.runA(VariableBindings.empty).value
      }
    }
  }

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
        ancestryGraph.nodes.values.map(_._1).filter(_.name == t.name).toSeq
      case t: GenericType =>
        for {
          generic <- ancestryGraph.nodes.values.map(_._1).filter(_.name == t.name).toSeq
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

  private def checkBindings(bindings: VariableBindings): Boolean = {
    bindings.bindings.values.forall { types =>
      types
        .sliding(2, 1)
        .forall {
          case a :: b :: Nil => a.dri == b.dri
          case _             => true
        }
    } && !TypeVariablesGraph(bindings).hasCyclicDependency
  }
}

case class TypeVariablesGraph(variableBindings: VariableBindings) {
  val dependencyGraph: Map[DRI, Seq[DRI]] = variableBindings.bindings.view
    .mapValues(_.flatMap {
      case g: GenericType => retrieveVariables(g)
      case _ => Seq()
    }.distinct)
    .toMap

  private def retrieveVariables(t: Type): Seq[DRI] = t match {
    case g: GenericType  => g.params.map(_.typ).flatMap(retrieveVariables)
    case t: TypeVariable => Seq(t.dri.get)
    case _ => Seq()
  }

  def hasCyclicDependency: Boolean = {
    case class DfsState(visited: Set[DRI] = Set.empty, stack: Set[DRI] = Set.empty)

    def loop(current: DRI): State[DfsState, Boolean] =
      for {
        dfsState <- State.get[DfsState]
        cycle    = dfsState.stack.contains(current)
        visited  = dfsState.visited.contains(current)
        newState = dfsState.modifyAll(_.visited, _.stack).using(_ + current)
        _ <- State.set[DfsState](newState)
        f <- if (!visited)
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

case class AncestryGraph(nodes: Map[DRI, (Type, Seq[Type])]) extends FluffServiceOps {

  //TODO #55 Consider making a common context for constraints from both signatures
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
        State.modify[VariableBindings](_.add(typ.dri.get, supr).add(supr.dri.get, typ)) >>
          State.pure(typConstraints == suprConstraints) // TODO #56 Better 'equality' between two TypeVariables
      case (typ, supr: TypeVariable) =>
        val constraints = context.constraints.get(supr.name.name).toSeq.flatten.toList
        for {
          _ <- State.modify[VariableBindings](_.add(supr.dri.get, typ))
          checks <- constraints.traverse { t =>
            isSubType(typ, t, context)
          }
        } yield checks.forall(identity)
      case (typ: TypeVariable, supr) =>
        val constraints = context.constraints.get(typ.name.name).toSeq.flatten.toList
        for {
          _ <- State.modify[VariableBindings](_.add(typ.dri.get, supr))
          checks <- constraints.traverse { t =>
            isSubType(t, supr, context)
          }
        } yield constraints.isEmpty || checks.exists(identity)
      case (typ: GenericType, _) if typ.isVariable =>
        State.pure(true) //TODO #58 Support for TypeVariables as GenericTypes
      case (_, supr: GenericType) if supr.isVariable =>
        State.pure(true) //TODO #58 Support for TypeVariables as GenericTypes
      case (typ, supr) if typ.dri == supr.dri => checkTypeParamsByVariance(typ, supr, context)
      case (typ, supr) =>
        typ.dri.fold {
          // TODO having an empty dri here shouldn't be possible, after fixing db, this should be refactored
          val ts = nodes.values.filter(_._1.name == typ.name).map(t => specializeType(typ, t._1)).toList
          ts.traverse(n => isSubType(n, supr, context)).map(_.exists(identity))
        } { dri =>
          if (nodes.contains(dri))
            specializeParents(typ, nodes(dri)).toList
              .traverse(isSubType(_, supr, context))
              .map(_.exists(identity))
          else State.pure(false) //TODO remove when everything is correctly resolved
        }
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
        State.pure[VariableBindings, Boolean](typParam == suprParam)
    }
  }

  private def writeVariancesFromDRI: Type => Type = {
    case typ: GenericType =>
      typ
        .modify(_.params)
        .using { params =>
          if (typ.dri.nonEmpty && nodes.contains(typ.dri.get)) {
            params.zip(nodes(typ.dri.get)._1.params).map {
              case (t, v) => zipVariance(t.typ, v)
            }
          } else params
        }
    case typ => typ
  }
}

trait FluffServiceOps {
  def zipVariance(typ: Type, v: Variance): Variance = {
    v match {
      case _: Contravariance => Contravariance(typ)
      case _: Covariance     => Covariance(typ)
      case _: Invariance     => Invariance(typ)
    }
  }
}

case class VariableBindings(bindings: Map[DRI, Seq[Type]]) {
  def add(dri: DRI, typ: Type): VariableBindings = {
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
