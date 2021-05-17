package org.virtuslab.inkuire.engine.common.model

case class Match(
  prettifiedSignature: String,
  functionName:        String,
  packageLocation:     String,
  pageLocation:        String
)

case class OutputFormat(
  query:   String,
  matches: List[Match]
)
