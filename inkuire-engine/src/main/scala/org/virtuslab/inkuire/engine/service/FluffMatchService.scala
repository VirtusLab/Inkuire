package org.virtuslab.inkuire.engine.service
//
//import org.virtuslab.inkuire.engine.model.{DRI, ExternalSignature, InkuireDb, Signature, Type}
//
//class FluffMatchService(val inkuireDb: InkuireDb) extends BaseMatchService {
//
//  val ancestryGraph: AncestryGraph = AncestryGraph(inkuireDb.types)
//
//  override def |??|(signature: Signature): Seq[ExternalSignature] = {
//    inkuireDb.functions.filter { eSgn =>
//      val okReceiver = checkReceiver(eSgn, signature)
//      val okParams   = checkParams(eSgn, signature)
//      val okResult   = checkResult(eSgn, signature)
//      okReceiver && okParams && okResult
//    }
//  }
//
//  private def checkReceiver(eSgn: ExternalSignature, signature: Signature): Boolean = {
//    //TODO check constraints
//    (eSgn.signature.receiver.isEmpty && signature.receiver.isEmpty) || ancestryGraph.isSubType(
//      typ  = eSgn.signature.receiver.get,
//      supr = signature.receiver.get
//    )
//  }
//
//  private def checkParams(eSgn: ExternalSignature, signature: Signature): Boolean = {
//    //TODO disregard order
//    //TODO check constraints
//    eSgn.signature.arguments.zip(signature.arguments).forall {
//      case (eSgnType, sgnType) =>
//        ancestryGraph.isSubType(typ = eSgnType, supr = sgnType)
//    }
//  }
//
//  private def checkResult(eSgn: ExternalSignature, signature: Signature): Boolean = {
//    //TODO check constraints
//    ancestryGraph.isSubType(
//      typ  = signature.result,
//      supr = eSgn.signature.result
//    )
//  }
//}
//
//case class AncestryGraph(nodes: Map[DRI, (Type, Seq[Type])]) {
//  def isSubType(typ: Type, supr: Type): Boolean =
//    typ == supr ||
//      specializeParents(
//        typ,
//        typ.dri.fold(nodes.values.map(_._1).filter(_ == typ).map(t => t -> nodes(t.dri.get)))(nodes(_))
//      )
//
//  private def specializeParents(concreteType: Type, node: (Type, Seq[Type])): Seq[Type] =
//    ???
//}
