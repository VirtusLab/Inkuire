package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model._
import com.softwaremill.quicklens._

case class TypingState(
  variableBindings: VariableBindings
) {
  def addBinding(dri: ITID, typ: Type): TypingState =
    this.modify(_.variableBindings).using(_.add(dri, typ))
}

object TypingState {
  def empty: TypingState = TypingState(VariableBindings.empty)
}
