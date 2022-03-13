package org.virtuslab.inkuire.engine.impl.model

case class AnnotatedSignature(
  signature:   Signature,
  name:        String,
  packageName: String,
  uri:         String,
  entryType:   String
) {
  val uuid: String = entryType + packageName + name + uri
}
