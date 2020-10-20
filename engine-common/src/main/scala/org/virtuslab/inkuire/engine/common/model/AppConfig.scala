package org.virtuslab.inkuire.engine.common.model

import org.virtuslab.inkuire.engine.common.utils.syntax._
import cats.implicits._

case class AppConfig(
  address:            Address,
  port:               Port,
  dbPaths:            Seq[DbPath],
  ancestryGraphPaths: Seq[AncestryGraphPath]
)

object AppConfig {
  def create(args: List[AppParam]): Either[String, AppConfig] = {
    val address            = args.collectFirst { case a: Address           => a }.toRight(noConfigFoundString("address"))
    val port               = args.collectFirst { case p: Port              => p }.toRight(noConfigFoundString("port"))
    val dbPaths            = args.collect { case a:      DbPath            => a }.right[String]
    val ancestryGraphPaths = args.collect { case p:      AncestryGraphPath => p }.right[String]
    (address, port, dbPaths, ancestryGraphPaths).mapN(AppConfig.apply)
  }

  private def noConfigFoundString(paramName: String) =
    s"No value for config parameter '$paramName' found"
}

trait AppParam extends Any
case class Address(address:        String) extends AnyVal with AppParam
case class Port(port:              Int) extends AnyVal with AppParam
case class DbPath(path:            String) extends AnyVal with AppParam
case class AncestryGraphPath(path: String) extends AnyVal with AppParam

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
