package org.virtuslab.inkuire.engine.common.model

case class Match(
  prettifiedSignature: String,
  functionName:        String,
  packageLocation:     String,
  pageLocation:        String,
  entryType:           String
)

sealed trait OutputFormat

case class ResultFormat(
  query:   String,
  matches: List[Match]
) extends OutputFormat

case object EndFormat extends OutputFormat
