package org.virtuslab.inkuire.engine.common.parser

import cats.Monoid
import org.virtuslab.inkuire.engine.common.model.Type
import org.virtuslab.inkuire.engine.common.model._

import scala.util.parsing.combinator.RegexParsers

abstract class BaseSignatureParser extends RegexParsers {

  def identifier: Parser[String] = """[A-Za-z]\w*""".r

  def nullability: Parser[Boolean] = "?" ^^^ true | "" ^^^ false

  def list[A](typ: Parser[A]): Parser[Seq[A]] =
    (typ <~ ",") ~ list(typ) ^^ { case head ~ tail => head +: tail } |
      typ ^^ (Seq(_))

  def empty[T](implicit monoid: Monoid[T]): Parser[T] = "" ^^^ monoid.empty

  def genericType: Parser[Type]

  def functionType: Parser[Type]

  def signature: Parser[ParsedSignature]

}
