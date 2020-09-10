package org.virtuslab.inkuire.engine.model

import org.virtuslab.inkuire.engine.utils.syntax._

case class AppConfig(
  address:            Address,
  port:               Port,
  bdPaths:            Seq[DbPath],
  ancestryGraphPaths: Seq[AncestryGraphPath]
)

object AppConfig {
  def create(args: List[AppParam]): AppConfig = {
    val addresses          = args.collect { case a: Address => a }.head
    val ports              = args.collect { case p: Port => p }.head
    val bdPaths            = args.collect { case a: DbPath => a }
    val ancestryGraphPaths = args.collect { case p: AncestryGraphPath => p }
    AppConfig(addresses, ports, bdPaths, ancestryGraphPaths)
  }
}

trait AppParam
case class Address(address:        String) extends AppParam
case class Port(port:              Int) extends AppParam
case class DbPath(path:            String) extends AppParam
case class AncestryGraphPath(path: String) extends AppParam

object AppParam {
  def parseCliOption(opt: String, v: String): Either[String, AppParam] =
    opt match {
      case "-d" | "--database" => DbPath(v).right
      case "-a" | "--ancestry" => AncestryGraphPath(v).right
      case "--address"         => Address(v).right
      case "-p" | "--port"     => Port(v.toInt).right
      case _                   => s"Wrong option $opt".left
    }
}
