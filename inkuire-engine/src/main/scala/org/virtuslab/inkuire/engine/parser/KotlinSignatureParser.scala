package org.virtuslab.inkuire.engine.parser

import org.virtuslab.inkuire.engine.model.{ConcreteType, GenericType, Signature, Type}

import scala.util.parsing.combinator._

class KotlinSignatureParser extends RegexParsers {
  def identifier: Parser[String] = """[A-Za-z]\w*""".r
  def singleType: Parser[Type] =
    identifier ~ ('<' ~> types <~ '>') ^^ { case genericType ~ types => GenericType(genericType, types) } | identifier ^^ ConcreteType
  def types: Parser[Seq[Type]] =
    (singleType <~ ',') ~ types ^^ { case typ ~ types => typ +: types } | singleType ^^ (Seq(_)) | "" ^^ Function.const(Seq.empty[Type])
  def signature: Parser[Signature] =
    singleType ~ (".(" ~> types <~ ')') ~ ("->" ~> singleType) ^^ { case receiver ~ args ~ result => Signature(receiver, args, result) }
}

object KotlinSignatureParser extends KotlinSignatureParser {
  def parse(str: String): Either[String, Signature] = {
    parse(signature, str) match {
      case Success(matched, _) => Right(matched)
      case Failure(msg, _) => Left(msg)
      case Error(msg, _) => Left(msg)
    }
  }
}