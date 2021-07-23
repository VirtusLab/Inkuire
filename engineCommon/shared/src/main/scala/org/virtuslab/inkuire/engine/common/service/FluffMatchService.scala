package org.virtuslab.inkuire.engine.common.service

import cats.data.{State, StateT}
import org.virtuslab.inkuire.engine.common.model._
import com.softwaremill.quicklens._
import cats.implicits._
import scala.util.Random

class FluffMatchService(val inkuireDb: InkuireDb) extends BaseMatchService with VarianceOps {

  val ancestryGraph: AncestryGraph = AncestryGraph(inkuireDb.types, inkuireDb.conversions, inkuireDb.typeAliases)

  implicit class TypeOps(sgn: Signature) {
    def canSubstituteFor(supr: Signature): Boolean = {
      ancestryGraph
        .checkTypesWithVariances(
          sgn.typesWithVariances,
          supr.typesWithVariances,
          sgn.context |+| supr.context
        )
        .flatMap { okTypes =>
          State.get[VariableBindings].map { bindings =>
            if (okTypes) checkBindings(bindings)
            else false
          }
        }
        .runA(VariableBindings.empty)
        .value
    }
  }

  override def |?|(resolveResult: ResolveResult)(against: ExternalSignature): Boolean = {
    resolveResult.signatures.exists(against.signature.canSubstituteFor(_))
  }

  override def |??|(resolveResult: ResolveResult): Seq[ExternalSignature] = {
    val actualSignatures = resolveResult.signatures.foldLeft(resolveResult.signatures) {
      case (acc, against) =>
        acc.filter { sgn =>
          sgn == against || !(sgn.canSubstituteFor(against) && !against.canSubstituteFor(sgn))
        }
    }
    val actualSignaturesSize = actualSignatures.headOption.map(_.typesWithVariances.size)
    inkuireDb.functions
      .filter(fun => Some(fun.signature.typesWithVariances.size) == actualSignaturesSize)
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
              a.params.size == b.params.size &&
              a.params.map(_.typ).zip(b.params.map(_.typ)).forall {
                case (a: Type, b: Type) => a.itid == b.itid
                case _ => false
              }
          case _ :: _ :: Nil => false
          case _             => true
        }
    } && !TypeVariablesGraph(bindings).hasCyclicDependency
  }
}
