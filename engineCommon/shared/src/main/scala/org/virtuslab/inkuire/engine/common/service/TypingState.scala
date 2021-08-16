package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model._

case class TypingState(
  variableBindings: VariableBindings,
  visitedTypes: Set[TypeLike] //TODO Hmmmmmmmmmmmmmmmmmmm
) {
  def addBinding(dri: ITID, typ: Type): TypingState =
    this.copy(variableBindings = variableBindings.add(dri, typ))

  def addVisited(visited: Set[TypeLike]): TypingState =
    this.copy(visitedTypes = this.visitedTypes ++ visited)

  def removeVisited(visited: Set[TypeLike]): TypingState =
    this.copy(visitedTypes = this.visitedTypes -- visited)

  def visitedContains(t: TypeLike): Boolean =
    visitedTypes.contains(t)
}

object TypingState {
  def empty: TypingState = TypingState(VariableBindings.empty, Set.empty)
}