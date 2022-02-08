package org.virtuslab.inkuire.http

import org.virtuslab.inkuire.engine.impl.utils.Monoid

case class AppConfig(
  address:      Option[String] = None,
  port:         Option[Int] = None,
  inkuirePaths: Seq[String] = Seq.empty
) {
  def getAddress: String = address.getOrElse("0.0.0.0")
  def getPort:    Int    = port.getOrElse(8080)
}

object AppConfig {
  def empty: AppConfig = AppConfig(
    address = None,
    port = None,
    inkuirePaths = Seq.empty
  )

  implicit val appConfigMonoid: Monoid[AppConfig] = new Monoid[AppConfig] {
    def empty: AppConfig = AppConfig.empty
    def mappend(x: AppConfig, y: AppConfig): AppConfig =
      AppConfig(
        address = x.address.orElse(y.address),
        port = x.port.orElse(y.port),
        inkuirePaths = x.inkuirePaths ++ y.inkuirePaths
      )
  }

  def parseCliOption(opt: String, v: String): AppConfig =
    opt match {
      case "-a" | "--address" => AppConfig(address = Some(v))
      case "-p" | "--port"    => AppConfig(port = Some(v.toInt))
      case "-i" | "--inkuire" => AppConfig(inkuirePaths = Seq(v))
      case o =>
        println(s"Inkuire ignored wrong option: $o")
        AppConfig.empty
    }
}
