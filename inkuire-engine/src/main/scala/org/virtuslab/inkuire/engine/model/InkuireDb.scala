package org.virtuslab.inkuire.engine.model

case class InkuireDb(
  functions: Seq[ExternalSignature],
  types: Map[Type, Set[Type]]
)

object InkuireDb {
  def read(path: String): InkuireDb = {
    // TODO read db from given path
    new InkuireDb(Seq.empty, Map.empty)
  }
}

case class ExternalSignature(
  signature: Signature,
  name: String,
  uri: String
)