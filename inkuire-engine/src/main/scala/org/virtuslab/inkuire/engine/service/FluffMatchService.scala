package org.virtuslab.inkuire.engine.service

import org.virtuslab.inkuire.engine.model._
import com.softwaremill.quicklens._

class FluffMatchService(val inkuireDb: InkuireDb) extends BaseMatchService {

  val ancestryGraph: AncestryGraph = AncestryGraph(inkuireDb.types)

  override def |??|(signature: Signature): Seq[ExternalSignature] = {
    inkuireDb.functions.filter { eSgn =>
      val okReceiver = checkReceiver(eSgn, signature)
      val okParams   = checkArguments(eSgn, signature)
      val okResult   = checkResult(eSgn, signature)
      okReceiver && okParams && okResult
    }
  }

  private def checkReceiver(eSgn: ExternalSignature, signature: Signature): Boolean = {
    //TODO check constraints
    if (eSgn.signature.receiver.nonEmpty && signature.receiver.nonEmpty) {
      ancestryGraph.isSubType(
        typ  = signature.receiver.get,
        supr = eSgn.signature.receiver.get
      )
    } else eSgn.signature.receiver.nonEmpty == signature.receiver.nonEmpty
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

  def isSubType(typ: Type, supr: Type): Boolean =
    if (!supr.isInstanceOf[TypeVariable] && supr.dri.isEmpty)
      nodes.values.map(_._1).filter(_.name == supr.name).exists(isSubType(typ, _))
    else if (supr.isInstanceOf[TypeVariable]) {
      true // TODO && same as below, translate constraints to calls to isSubType, like parents
    } else if (typ.isInstanceOf[TypeVariable]) {
      true // TODO && actually constraints should be checked here, maybe map constraints just like parents
    } else {
      (typ.dri == supr.dri && checkTypeParamsByVariance(typ, supr)) ||
      typ.dri.fold {
        val ts = nodes.values.filter(_._1.name == typ.name).map(t => specializeType(typ, t._1))
        ts.exists(n => isSubType(n, supr))
      } { dri =>
        specializeParents(typ, nodes(dri)).exists(isSubType(_, supr))
      }
    }

  private def specializeParents(concreteType: Type, node: (Type, Seq[Type])): Seq[Type] = {
    val bindings = node._1.params.map(_.name).zip(concreteType.params).toMap
    node._2.map {
      case t: GenericType => t.modify(_.params.each).using(p => bindings(p.name))
      case t => t
    }
  }

  private def specializeType(parsedType: Type, possibleMatch: Type): Type =
    possibleMatch match {
      case t: GenericType =>
        t.modify(_.params)
          .using(_.zip(parsedType.asInstanceOf[GenericType].params).map(p => specializeType(p._1, p._2)))
      case _ => possibleMatch
    }

  private def checkTypeParamsByVariance(typ: Type, supr: Type): Boolean =
    //TODO typ.params.zip(supr.params).map(isSubType(??? <- tu kolejnosc parametrów zalezy od wariancji))
    true
}
