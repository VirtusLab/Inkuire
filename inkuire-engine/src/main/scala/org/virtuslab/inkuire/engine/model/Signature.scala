package org.virtuslab.inkuire.engine.model

import org.virtuslab.inkuire.engine.model.TypeVariable

case class Signature(
  receiver: Type,
  arguments: Seq[Type],
  result: Type,
  context: SignatureContext
)

case class SignatureContext(
  vars: Set[TypeVariable],
  constraints: Map[TypeVariable, Set[Type]]
)

object SignatureContext {

  def empty: SignatureContext = {
    SignatureContext(
      Set.empty,
      Map.empty
    )
  }
}