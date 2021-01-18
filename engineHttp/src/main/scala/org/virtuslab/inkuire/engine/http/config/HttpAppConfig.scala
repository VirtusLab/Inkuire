package org.virtuslab.inkuire.engine.http.config

import org.virtuslab.inkuire.engine.common.model._
import cats.implicits._
import org.virtuslab.inkuire.engine.common.utils.syntax.AnyInkuireSyntax

case class HttpAppConfig(
  address:            Address,
  port:               Port,
  dbPaths:            Seq[DbPath],
  ancestryGraphPaths: Seq[AncestryGraphPath]
) extends AppConfig

object HttpAppConfig {
  def create(args: List[AppParam]): Either[String, AppConfig] = {
    val address            = args.collectFirst { case a: Address => a }.toRight(noConfigFoundString("address"))
    val port               = args.collectFirst { case p: Port => p }.toRight(noConfigFoundString("port"))
    val dbPaths            = Right(args.collect { case a: DbPath => a })
    val ancestryGraphPaths = Right(args.collect { case p: AncestryGraphPath => p })
    (address, port, dbPaths, ancestryGraphPaths).mapN(HttpAppConfig.apply)
  }

  def validate(appConfig: AppConfig): Either[String, HttpAppConfig] = appConfig match {
    case h: HttpAppConfig => h.right
    case _ => "Provided app config is not an instance of HttpAppConfig".left
  }

  private def noConfigFoundString(paramName: String) =
    s"No value for config parameter '$paramName' found"
}

case class Address(address: String) extends AnyVal with AppParam
case class Port(port: Int) extends AnyVal with AppParam
