package org.virtuslab.inkuire.engine.impl.service

import org.virtuslab.inkuire.engine.impl.model.Type
import org.virtuslab.inkuire.engine.impl.model._

import scala.util.parsing.combinator.RegexParsers

abstract class BaseSignatureParser extends RegexParsers {

  def identifier: Parser[String] = """[A-Za-z]\w*""".r

  def nullability: Parser[Boolean] = "?" ^^^ true | "" ^^^ false

  def list[A](typ: Parser[A]): Parser[Seq[A]] =
    (typ <~ ",") ~ list(typ) ^^ { case head ~ tail => head +: tail } |
      typ ^^ (Seq(_))

  def empty[A]: Parser[List[A]] = "" ^^^ List.empty

  def genericType: Parser[Type]

  def functionType: Parser[Type]

  def signature: Parser[ParsedSignature]

}
