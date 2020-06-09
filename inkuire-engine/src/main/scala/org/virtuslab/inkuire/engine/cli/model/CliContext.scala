package org.virtuslab.inkuire.engine.cli.model

import com.softwaremill.quicklens._

case class CliContext(
  dbPath: String
)

object CliContext {
  def empty: CliContext = CliContext("inkuire.json")
  def create(args: List[CliParam]): CliContext = {
    args.foldLeft(empty) {
      case (agg, SetDbPath(path)) => agg.modify(_.dbPath).setTo(path)
    }
  }
}