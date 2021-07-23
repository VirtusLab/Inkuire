package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model._
import cats.data.State
import cats.implicits._
import com.softwaremill.quicklens._

case class TypeVariablesGraph(variableBindings: VariableBindings) {
  val dependencyGraph: Map[ITID, Seq[ITID]] = variableBindings.bindings.view.mapValues {
    _.flatMap {
      case g: Type if g.params.nonEmpty => retrieveVariables(g)
      case _ => Seq()
    }.distinct
  }.toMap

  private def retrieveVariables(t: TypeLike): Seq[ITID] =
    t match {
      case t: Type if t.isVariable => Seq(t.itid.get)
      case g: Type                 => g.params.map(_.typ).flatMap(retrieveVariables)
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
