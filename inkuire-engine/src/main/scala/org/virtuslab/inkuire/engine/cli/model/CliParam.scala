package org.virtuslab.inkuire.engine.cli.model

import org.virtuslab.inkuire.engine.utils.syntax._

sealed trait CliParam
case class SetDbPath(path: String) extends CliParam

object CliParam {
  def parseCliOption(opt: String, v: String): Either[String, CliParam] =
    opt match {
      case "-d" | "--database" => SetDbPath(v).right
      case _                   => s"Wrong option $opt".left
    }
}
