package org.virtuslab.inkuire.engine.parser

import org.virtuslab.inkuire.engine.model.Type._
import scala.util.parsing.combinator.RegexParsers
import com.softwaremill.quicklens._
import org.virtuslab.inkuire.engine.model.{GenericType, Signature, SignatureContext, Type, TypeVariable, Unresolved}

class KotlinSignatureParser extends RegexParsers {

  def identifier: Parser[String] = """[A-Za-z]\w*""".r

  def nullability: Parser[Boolean] = "?" ^^^ true | "" ^^^ false

  def singleType: Parser[Type] =
    identifier ~ ("<" ~> types <~ ">") ~ nullability ^^ { case genericType ~ types ~ nullable => GenericType(Unresolved(genericType, nullable), types) } |
      identifier ~ nullability ^^ { case id ~ nullable => Unresolved(id, nullable) }

  def types: Parser[Seq[Type]] =
    (singleType <~ ",") ~ types ^^ { case typ ~ types => typ +: types } |
      singleType ^^ (Seq(_)) |
      "" ^^^ Seq.empty

  def whereClause: Parser[Seq[(TypeVariable, Type)]] = ???

  def typeVariables: Parser[Seq[TypeVariable]] =
    (identifier <~ "," ) ~ typeVariables ^^ { case typeVar ~ vars => typeVar.typeVariable +: vars } |
      identifier ^^ (v => Seq(v.typeVariable))

  def variables: Parser[Seq[TypeVariable]] =
    "<" ~> typeVariables <~ ">" |
      "" ^^^ Seq.empty

  def signature: Parser[Signature] =
    variables ~
      singleType ~
      ("." ~> "(" ~> types <~ ")") ~
      ("->" ~> singleType) ^^ { case typeVars ~ receiver ~ args ~ result => Signature(receiver, args, result, SignatureContext(typeVars.toSet, Map.empty)) }
}

object KotlinSignatureParser extends KotlinSignatureParser {
  def parse(str: String): Either[String, Signature] = {
    doParse(str).map { sgn =>
      val converter: Type => Type = resolve(sgn.context.vars)
      sgn
        .modifyAll(_.receiver, _.result).using(converter)
        .modify(_.arguments).using(_.map(converter))
    }
  }

  private def resolve(vars: Set[TypeVariable])(t: Type): Type =
    t match {
      case genType: GenericType =>
        val converter: Type => Type = resolve(vars)
        genType
          .modify(_.base).using(converter)
          .modify(_.params).using(_.map(converter))
      case _: Unresolved        =>
        vars.find(_.name == t.name).fold[Type](t.asConcrete)(Function.const(t.asVariable))
      case _                    => t
  }

  private def doParse(str: String): Either[String, Signature] = {
    parse(signature, str) match {
      case Success(matched, _) => Right(matched)
      case Failure(msg, _) => Left(msg)
      case Error(msg, _) => Left(msg)
    }
  }
}