package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.utils.State
import com.softwaremill.quicklens._
import org.virtuslab.inkuire.engine.common.model._

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
        _ <- State.put[DfsState](newState)
        f <-
          if (!visited)
            dependencyGraph
              .getOrElse(current, Seq())
              .toList
              .map(loop)
              .foldLeft(State.pure[DfsState, List[Boolean]](List.empty)) {
                case (acc, s) => acc.flatMap { accValue =>
                  s.map { sValue =>
                    accValue :+ sValue
                  }
                }
              }
          else State.pure[DfsState, List[Boolean]](List())
        _ <- State.modify[DfsState](s => s.modify(_.stack).using(_ - current))
      } yield cycle || f.exists(identity)

    dependencyGraph.keys.toList
      .map { v =>
        for {
          dfsState <- State.get[DfsState]
          flag <- if (dfsState.visited.contains(v)) State.pure[DfsState, Boolean](false) else loop(v)
        } yield flag
      }
      .foldLeft(State.pure[DfsState, List[Boolean]](List.empty)) {
        case (acc, s) => acc.flatMap { accValue =>
          s.map { sValue =>
            accValue :+ sValue
          }
        }
      }
      .map(_.exists(identity))
      .evalState(DfsState())
  }
}
