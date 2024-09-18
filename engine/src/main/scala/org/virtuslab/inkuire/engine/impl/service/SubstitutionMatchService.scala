package org.virtuslab.inkuire.engine.impl.service

import com.softwaremill.quicklens._
import org.virtuslab.inkuire.engine.api._
import org.virtuslab.inkuire.engine.impl.model._
import org.virtuslab.inkuire.engine.impl.utils.Monoid._
import org.virtuslab.inkuire.engine.impl.utils.State

class SubstitutionMatchService(val inkuireDb: InkuireDb) extends BaseMatchService with MatchingOps {

  val ancestryGraph: AncestryGraph =
    AncestryGraph(inkuireDb.types, inkuireDb.implicitConversions, inkuireDb.typeAliases)

  implicit class TypeOps(sgn: Signature) {
    def canSubstituteFor(supr: Signature): Boolean = {
      ancestryGraph
        .checkTypesWithVariances(
          sgn.typesWithVariances,
          supr.typesWithVariances,
          sgn.context <> supr.context
        )
        .flatMap { okTypes =>
          State.get[TypingState].map { typingState =>
            if (okTypes)
              checkBindings(typingState.variableBindings)
            else false
          }
        }
        .evalState(TypingState.empty)
    }
  }

  override def isMatch(resolveResult: ResolveResult)(against: AnnotatedSignature): Option[Signature] = {
    if (resolveResult.filters.canMatch(against))
      resolveResult.signatures
        .collectFirst {
          case s if against.signature.canSubstituteFor(s) => s
        }
    else
      None
  }

  override def findMatches(resolveResult: ResolveResult): Seq[(AnnotatedSignature, Signature)] = {
    val actualSignatures = resolveResult.signatures.foldLeft(resolveResult.signatures) {
      case (acc, against) =>
        acc.filter { sgn =>
          sgn == against || !sgn.canSubstituteFor(against) || against.canSubstituteFor(sgn)
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
    val bindingsCorrect = bindings.bindings.values.forall { types =>
      types
        .sliding(2, 1)
        .forall {
          case (a: Type) :: (b: Type) :: Nil =>
            val exactlyEqual = a == b
            // Disable this check, since it can't handle transitive relations
            // val relatedToEachOther =
            //   ancestryGraph.getAllParentsITIDs(a).contains(b.itid.get) ||
            //     ancestryGraph.getAllParentsITIDs(b).contains(a.itid.get)
            val sameSize = a.params.size == b.params.size
            val sameTypes = a.params.map(_.typ).zip(b.params.map(_.typ)).forall {
              case (a: Type, b: Type) => a.itid == b.itid
              case _                  => false
            }
            exactlyEqual || (sameSize && sameTypes)
          case _ => true
        }
    }
    val noCycles = !TypeVariablesGraph(bindings).hasCyclicDependency
    bindingsCorrect && noCycles
  }
}
