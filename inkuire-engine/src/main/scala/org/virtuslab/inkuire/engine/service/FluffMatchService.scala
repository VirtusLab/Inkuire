package org.virtuslab.inkuire.engine.service

import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.model._
import com.softwaremill.quicklens._
import org.virtuslab.inkuire.engine.cli.service.KotlinExternalSignaturePrettifier

//TODO add support for star projection
//TODO handle type variables, by binding all occurances to
class FluffMatchService(val inkuireDb: InkuireDb) extends BaseMatchService {

  val ancestryGraph: AncestryGraph = AncestryGraph(inkuireDb.types)

  override def |??|(signature: Signature): Seq[ExternalSignature] = {
    val signatures = resolveAllPossibleSignatures(signature)
    println(signatures.size.toString + "    " + signatures)
    inkuireDb.functions.filter { eSgn =>
      signatures.exists { sgn =>
        val okReceiver = checkReceiver(eSgn, sgn)
        val okParams   = checkArguments(eSgn, sgn)
        val okResult   = checkResult(eSgn, sgn)
        okReceiver && okParams && okResult
      }
    }
  }

  private def resolveAllPossibleSignatures(signature: Signature): Seq[Signature] = {
    for {
      receiver <- signature.receiver
        .fold[Seq[Option[Type]]](Seq(None))(resolvePossibleTypes(_).map(_.some))
      args <- resolveMultipleTypes(signature.arguments)
      result <- resolvePossibleTypes(signature.result)
    } yield
      signature
        .modify(_.receiver)
        .setTo(receiver)
        .modify(_.arguments)
        .setTo(args)
        .modify(_.result)
        .setTo(result)
  }

  private def resolvePossibleTypes(typ: Type): Seq[Type] = {
    typ match {
      case t: TypeVariable =>
        Seq(t)
      case t: ConcreteType =>
        println(ancestryGraph.nodes.values.map(_._1).filter(_.name == t.name).toSeq)
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

  private def checkReceiver(eSgn: ExternalSignature, signature: Signature): Boolean = {
    //TODO check constraints
    (eSgn.signature.receiver, signature.receiver) match {
      case (None, None) => true
      case (Some(eReceiver), Some(receiver)) =>
        ancestryGraph.isSubType(
          typ  = receiver,
          supr = eReceiver
        )
      case _ => false
    }
  }

  private def checkArguments(eSgn: ExternalSignature, signature: Signature): Boolean = {
    //TODO disregard order (maybe)
    //TODO check constraints
    eSgn.signature.arguments.size == signature.arguments.size &&
    eSgn.signature.arguments.zip(signature.arguments).forall {
      case (eSgnType, sgnType) =>
        ancestryGraph.isSubType(typ = sgnType, supr = eSgnType)
    }
  }

  private def checkResult(eSgn: ExternalSignature, signature: Signature): Boolean = {
    //TODO check constraints
    ancestryGraph.isSubType(
      typ  = eSgn.signature.result,
      supr = signature.result
    )
  }
}

case class AncestryGraph(nodes: Map[DRI, (Type, Seq[Type])]) {

  def isSubType(typ: Type, supr: Type): Boolean = {
    (typ, supr) match {
      case (_, supr: TypeVariable) =>
        true // TODO && same as below, translate constraints to calls to isSubType, like parents
      case (typ: TypeVariable, _) =>
        true // TODO && actually constraints should be checked here, maybe map constraints just like parents
      case (typ: GenericType, _) if typ.dri.isEmpty =>
        true //TODO this case indicates generic with base as variable, which isn't technically possible in Kotlin, but loosening this constraint should be considered
      case (_, supr: GenericType) if supr.dri.isEmpty =>
        true //TODO this case indicates generic with base as variable, which isn't technically possible in Kotlin, but loosening this constraint should be considered
      case (typ, supr) =>
        (typ.dri == supr.dri && checkTypeParamsByVariance(typ, supr)) ||
          typ.dri.fold {
            // TODO having an empty dri here shouldn't be possible, after fixing db, this should be refactored
            val ts = nodes.values.filter(_._1.name == typ.name).map(t => specializeType(typ, t._1))
            ts.exists(n => isSubType(n, supr))
          } { dri =>
            if (nodes.contains(dri)) specializeParents(typ, nodes(dri)).exists(isSubType(_, supr))
            else false //TODO remove when everything is correctly resolved
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

  private def checkTypeParamsByVariance(typ: Type, supr: Type): Boolean =
    //TODO typ.params.zip(supr.params).map(isSubType(??? <- tu kolejnosc parametrów zalezy od wariancji))
    true
}
