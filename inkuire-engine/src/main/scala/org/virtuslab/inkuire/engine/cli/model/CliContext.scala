package org.virtuslab.inkuire.engine.cli.model

import java.io.File
import java.nio.file.Paths

case class CliContext(
  dbFiles: List[File],
  ancestryFiles: List[File]
)

object CliContext {

  private def toFile(path: String) = Paths.get(path).toFile
  def empty: CliContext = CliContext(List.empty, List.empty)
  def create(args: List[CliParam]): CliContext = {

    val dbFiles = args.collect { case DbPath(path) => toFile(path) }
    val ancestryGraphFiles = args.collect { case AncestryGraphPath(path) => toFile(path) }

    CliContext(dbFiles, ancestryGraphFiles)
  }
}