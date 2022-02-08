package org.virtuslab.inkuire.engine.model

case class ExternalSignature(
  signature:   Signature,
  name:        String,
  packageName: String,
  uri:         String,
  entryType:   String
) {
  val uuid: String = entryType + packageName + name + uri
}
