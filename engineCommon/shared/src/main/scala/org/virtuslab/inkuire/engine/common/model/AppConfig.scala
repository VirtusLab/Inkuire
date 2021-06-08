package org.virtuslab.inkuire.engine.common.model

import org.virtuslab.inkuire.engine.common.utils.syntax._
import cats.implicits._

case class AppConfig(
  address:      Address,
  port:         Port,
  inkuirePaths: Seq[InkuirePath]
)

object AppConfig {
  def create(args: List[AppParam]): AppConfig = {
    val address      = args.collectFirst { case a: Address => a }.getOrElse(Address("0.0.0.0"))
    val port         = args.collectFirst { case p: Port => p }.getOrElse(Port(8080))
    val inkuirePaths = args.collect { case i: InkuirePath => i }
    AppConfig(address, port, inkuirePaths)
  }
}

trait AppParam extends Any
case class Address(address: String) extends AnyVal with AppParam
case class Port(port: Int) extends AnyVal with AppParam
case class InkuirePath(path: String) extends AnyVal with AppParam

object AppParam {
  def parseCliOption(opt: String, v: String): Either[String, AppParam] =
    opt match {
      case "-a" | "--address" => Address(v).right
      case "-p" | "--port"    => Port(v.toInt).right
      case "-i" | "--inkuire" => InkuirePath(v).right
      case _                  => s"Wrong option $opt".left
    }
}
