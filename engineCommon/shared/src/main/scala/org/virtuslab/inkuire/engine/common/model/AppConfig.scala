package org.virtuslab.inkuire.engine.common.model

import org.virtuslab.inkuire.engine.common.utils.syntax._
import cats.implicits._
import cats.kernel.Monoid

case class AppConfig(
  address:      Option[String],
  port:         Option[Int],
  inkuirePaths: Seq[String]
) {
  def getAddress = address.getOrElse("0.0.0.0")
  def getPort = port.getOrElse(8080)
}

object AppConfig {
  implicit val appConfigMonoid = new Monoid[AppConfig] {
    def empty: AppConfig = AppConfig(
      address = None,
      port = None,
      inkuirePaths = Seq.empty
    )
    def combine(x: AppConfig, y: AppConfig): AppConfig =
      AppConfig(
        address = x.address.orElse(y.address),
        port = x.port.orElse(y.port),
        inkuirePaths = x.inkuirePaths ++ y.inkuirePaths
      )
  }

  def parseCliOption(opt: String, v: String): AppConfig =
    opt match {
      case "-a" | "--address" => Monoid.empty[AppConfig].copy(address = Some(v))
      case "-p" | "--port"    => Monoid.empty[AppConfig].copy(port = Some(v.toInt))
      case "-i" | "--inkuire" => Monoid.empty[AppConfig].copy(inkuirePaths = Seq(v))
      case o                  =>
        println(s"Inkuire ignored wrong option: $o")
        Monoid.empty[AppConfig]
    }
}
