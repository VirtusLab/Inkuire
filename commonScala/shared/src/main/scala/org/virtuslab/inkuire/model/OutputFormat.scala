package org.virtuslab.inkuire.model

case class Match(
  prettifiedSignature: String,
  functionName:        String,
  localization:        String
)

case class OutputFormat(
  query:   String,
  matches: List[Match]
)
