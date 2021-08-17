package org.virtuslab.inkuire.engine.common.service

import cats.data.{State, StateT}
import org.virtuslab.inkuire.engine.common.model._
import com.softwaremill.quicklens._
import cats.implicits._
import scala.util.Random

class FluffMatchService(val inkuireDb: InkuireDb) extends BaseMatchService with VarianceOps {

  val ancestryGraph: AncestryGraph = AncestryGraph(inkuireDb.types, inkuireDb.implicitConversions, inkuireDb.typeAliases)

  val functions = inkuireDb.functions.flatMap { func =>
    val fromConversions = inkuireDb.implicitConversions.filter { ic =>
      func.signature.receiver.nonEmpty && ancestryGraph.isSubTypeOfIC(ic._2, func.signature.receiver.get.typ)
    }.map { ic =>
      changeReceiver(func, ic._1, ic._2)
    }
    Seq(func) :+ fromConversions
  }

  def changeReceiver(func: ExternalSignature, to: TypeLike, from: Type): ExternalSignature = {
    val boundVars: List[(Type, Type)] = ancestryGraph.boundVarsIC(func.signature.receiver.get.typ, from)
    val varBindings: Map[ITID, Type] = boundVars.map { case (key, v) => key.itid.get -> v }.toMap
    func
      .modify(_.signature.receiver.each.typ).setTo(to)
      .modify(_.signature.arguments.each.typ).using(ancestryGraph.substituteBindings(_, varBindings))
      .modify(_.signature.result.typ).using(ancestryGraph.substituteBindings(_, varBindings))
  }

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

  override def isMatch(resolveResult: ResolveResult)(against: ExternalSignature): Boolean = {
    resolveResult.signatures.exists(against.signature.canSubstituteFor(_))
  }

  override def findMatches(resolveResult: ResolveResult): Seq[ExternalSignature] = {
    val actualSignatures = resolveResult.signatures.foldLeft(resolveResult.signatures) {
      case (acc, against) =>
        acc.filter { sgn =>
          sgn == against || !(sgn.canSubstituteFor(against) && !against.canSubstituteFor(sgn))
        }
    }
    val actualSignaturesSize = actualSignatures.headOption.map(_.typesWithVariances.size)
    inkuireDb.functions
      .filter(fun => Some(fun.signature.typesWithVariances.size) == actualSignaturesSize)
      .filter(fun => fun.name == "pipe")
      .filter(isMatch(resolveResult.modify(_.signatures).setTo(actualSignatures)))
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
