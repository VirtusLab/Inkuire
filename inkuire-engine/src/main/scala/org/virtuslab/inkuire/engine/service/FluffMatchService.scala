package org.virtuslab.inkuire.engine.service

import cats.data.State
import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.model._
import com.softwaremill.quicklens._
import cats.implicits._

//TODO add support for star projection
//TODO handle case where one variable depends on the other like e.g. <A, B : List<A>> A.() -> B
class FluffMatchService(val inkuireDb: InkuireDb) extends BaseMatchService {

  val parsedDriPrefix = "iri-"

  val ancestryGraph: AncestryGraph = AncestryGraph(inkuireDb.types)

  override def |??|(signature: Signature): Seq[ExternalSignature] = {
    val signatures = resolveAllPossibleSignatures(signature)
    inkuireDb.functions.filter { eSgn =>
      signatures.exists { sgn =>
        val ok = for {
          okReceiver <- checkReceiver(eSgn, sgn)
          okParams <- checkArguments(eSgn, sgn)
          okResult <- checkResult(eSgn, sgn)
          bindings <- State.get[VariableBindings]
          okBindings = checkBindings(bindings)
        } yield okReceiver && okParams && okResult && okBindings
        ok.runA(VariableBindings.empty).value
      }
    }
  }

  private def resolveAllPossibleSignatures(signature: Signature): Seq[Signature] = {
    for {
      receiver <- signature.receiver
        .fold[Seq[Option[Type]]](Seq(None))(resolvePossibleTypes(_).map(_.some))
      args <- resolveMultipleTypes(signature.arguments)
      result <- resolvePossibleTypes(signature.result)
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
    typ match {
      case t: TypeVariable =>
        Seq(
          t.modify(_.dri)
            .setTo(DRI(None, None, None, parsedDriPrefix + t.name.name).some)
        )
      case t: ConcreteType =>
        ancestryGraph.nodes.values.map(_._1).filter(_.name == t.name).toSeq
      case t: GenericType =>
        for {
          base <- resolvePossibleTypes(t.base)
          params <- resolveMultipleTypes(t.params)
        } yield copyDRI(t.modify(_.params).setTo(params), base.dri)
      case StarProjection => ??? //TODO czeba wzgledem wariancji (kontra -> Nothing, ko -> Any?)
    }
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

  private def checkReceiver(eSgn: ExternalSignature, signature: Signature): State[VariableBindings, Boolean] = {
    (eSgn.signature.receiver, signature.receiver) match {
      case (None, None) => State.pure(true)
      case (Some(eReceiver), Some(receiver)) =>
        ancestryGraph.isSubType(
          typ         = receiver,
          supr        = eReceiver,
          typContext  = signature.context,
          suprContext = eSgn.signature.context
        )
      case _ => State.pure(false)
    }
  }

  private def checkArguments(eSgn: ExternalSignature, signature: Signature): State[VariableBindings, Boolean] = {
    //TODO #54 Consider disregarding arguments order in FluffMatchService
    eSgn.signature.arguments
      .zip(signature.arguments)
      .toList
      .traverse {
        case (eSgnType, sgnType) =>
          ancestryGraph.isSubType(
            typ         = sgnType,
            supr        = eSgnType,
            typContext  = signature.context,
            suprContext = eSgn.signature.context
          )
      }
      .map(_.forall(identity) && eSgn.signature.arguments.size == signature.arguments.size)
  }

  private def checkResult(eSgn: ExternalSignature, signature: Signature): State[VariableBindings, Boolean] = {
    ancestryGraph.isSubType(
      typ         = eSgn.signature.result,
      supr        = signature.result,
      typContext  = eSgn.signature.context,
      suprContext = signature.context
    )
  }

  private def checkBindings(bindings: VariableBindings): Boolean = {
    //TODO can be done better
    bindings.bindings.values.forall { types =>
      types
        .sliding(2, 1)
        .forall {
          case a :: b :: Nil => a == b
          case _             => true
        }
    }
  }
}

case class AncestryGraph(nodes: Map[DRI, (Type, Seq[Type])]) {

  //TODO #55 Consider making a common context for constraints from both signatures
  def isSubType(
    typ:         Type,
    supr:        Type,
    typContext:  SignatureContext,
    suprContext: SignatureContext
  ): State[VariableBindings, Boolean] = {
    (typ, supr) match {
      case (typ: TypeVariable, supr: TypeVariable) =>
        val typConstraints  = typContext.constraints.get(typ.name.name).toSeq.flatten
        val suprConstraints = suprContext.constraints.get(supr.name.name).toSeq.flatten
        State.modify[VariableBindings](_.add(typ.dri.get, supr).add(supr.dri.get, typ)) >>
          State.pure(typConstraints == suprConstraints) // TODO #56 Better 'equality' between two TypeVariables
      case (typ, supr: TypeVariable) =>
        val constraints = suprContext.constraints.get(supr.name.name).toSeq.flatten.toList
        for {
          _ <- State.modify[VariableBindings](_.add(supr.dri.get, typ))
          checks <- constraints.traverse { t =>
            isSubType(typ, t, typContext, suprContext)
          }
        } yield checks.forall(identity)
      case (typ: TypeVariable, supr) =>
        val constraints = typContext.constraints.get(typ.name.name).toSeq.flatten.toList
        for {
          _ <- State.modify[VariableBindings](_.add(typ.dri.get, supr))
          checks <- constraints.traverse { t =>
            isSubType(t, supr, typContext, suprContext)
          }
        } yield constraints.isEmpty || checks.exists(identity)
      case (typ: GenericType, _) if typ.isVariable =>
        State.pure(true) //TODO #58 Support for TypeVariables as GenericTypes
      case (_, supr: GenericType) if supr.isVariable =>
        State.pure(true) //TODO #58 Support for TypeVariables as GenericTypes
      case (typ, supr) if typ.dri == supr.dri =>
        checkTypeParamsByVariance(typ, supr)
      case (typ, supr) =>
        typ.dri.fold {
          // TODO having an empty dri here shouldn't be possible, after fixing db, this should be refactored
          val ts = nodes.values.filter(_._1.name == typ.name).map(t => specializeType(typ, t._1)).toList
          ts.traverse(n => isSubType(n, supr, typContext, suprContext)).map(_.exists(identity))
        } { dri =>
          if (nodes.contains(dri))
            specializeParents(typ, nodes(dri)).toList
              .traverse(isSubType(_, supr, typContext, suprContext))
              .map(_.exists(identity))
          else State.pure(false) //TODO remove when everything is correctly resolved
        }
    }
  }

  private def specializeParents(concreteType: Type, node: (Type, Seq[Type])): Seq[Type] = {
    val bindings = node._1.params.map(_.name).zip(concreteType.params).toMap
    node._2.map {
      case t: GenericType if !t.base.isInstanceOf[TypeVariable] =>
        t.modify(_.params.each).using(p => bindings.getOrElse(p.name, p))
      case t => t
    }
  }

  private def specializeType(parsedType: Type, possibleMatch: Type): Type =
    (parsedType, possibleMatch) match {
      case (pT: GenericType, t: GenericType) =>
        t.modify(_.params)
          .using(_.zip(pT.params).map(p => specializeType(p._1, p._2)))
      case _ => possibleMatch
    }

  private def checkTypeParamsByVariance(typ: Type, supr: Type): State[VariableBindings, Boolean] =
    //TODO typ.params.zip(supr.params).map(isSubType(??? <- tu kolejnosc parametrów zalezy od wariancji))
    State.pure(true)
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
