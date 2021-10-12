package org.virtuslab.inkuire.engine.common.service

import cats.data.State
import cats.implicits._
import com.softwaremill.quicklens._
import org.virtuslab.inkuire.engine.common.model._

class FluffMatchService(val inkuireDb: InkuireDb) extends BaseMatchService with MatchingOps {

  val ancestryGraph: AncestryGraph =
    AncestryGraph(inkuireDb.types, inkuireDb.implicitConversions, inkuireDb.typeAliases)

  implicit class TypeOps(sgn: Signature) {
    def canSubstituteFor(supr: Signature): Boolean = {
      ancestryGraph
        .checkTypesWithVariances(
          sgn.typesWithVariances,
          supr.typesWithVariances,
          sgn.context |+| supr.context
        )
        .flatMap { okTypes =>
          State.get[TypingState].map { typingState =>
            if (okTypes) checkBindings(typingState.variableBindings)
            else false
          }
        }
        .runA(TypingState.empty)
        .value
    }
  }

  override def isMatch(resolveResult: ResolveResult)(against: ExternalSignature): Option[Signature] = {
    if (resolveResult.filters.canMatch(against))
      resolveResult.signatures
        .collectFirst {
          case s if against.signature.canSubstituteFor(s) => s
        }
    else
      None
  }

  override def findMatches(resolveResult: ResolveResult): Seq[(ExternalSignature, Signature)] = {
    val actualSignatures = resolveResult.signatures.foldLeft(resolveResult.signatures) {
      case (acc, against) =>
        acc.filter { sgn =>
          sgn == against || !(sgn.canSubstituteFor(against) && !against.canSubstituteFor(sgn))
        }
    }
    val actualSignaturesSize = actualSignatures.headOption.map(_.typesWithVariances.size)
    val actualResolveResult  = resolveResult.modify(_.signatures).setTo(actualSignatures)
    resolveResult.filters
      .filterFrom(inkuireDb.functions)
      .filter(fun => Some(fun.signature.typesWithVariances.size) == actualSignaturesSize)
      .map(fun => fun -> isMatch(actualResolveResult)(fun))
      .collect {
        case (fun, Some(matching)) => fun -> matching
      }
      .distinctBy(_._1.uuid)
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
          case _             => true
        }
    } && !TypeVariablesGraph(bindings).hasCyclicDependency
  }
}
