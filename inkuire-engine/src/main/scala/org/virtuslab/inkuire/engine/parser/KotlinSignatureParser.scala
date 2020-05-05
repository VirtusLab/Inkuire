package org.virtuslab.inkuire.engine.parser

import org.virtuslab.inkuire.engine.model.Type._
import scala.util.parsing.combinator.RegexParsers
import com.softwaremill.quicklens._
import org.virtuslab.inkuire.engine.model.{GenericType, Signature, SignatureContext, Type, Unresolved}
import org.virtuslab.inkuire.engine.utils.syntax._
import cats.instances.all._
import cats.syntax.all._

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

  def constraints: Parser[Seq[(String, Type)]] =
    (identifier <~ ":") ~ singleType ~ ("," ~> constraints) ^^ { case id ~ typ ~ consts => (id, typ) +: consts } |
    (identifier <~ ":") ~ singleType ^^ { case id ~ typ => Seq((id, typ)) }

  def whereClause: Parser[Map[String, Seq[Type]]] = "where" ~> constraints ^^ (_.groupBy(_._1).map { case (k, v) => (k, v.map(_._2)) }) | "" ^^^ Map.empty

  def typeVariables: Parser[Seq[String]] =
    (identifier <~ "," ) ~ typeVariables ^^ { case typeVar ~ vars => typeVar +: vars } |
      identifier ^^ (Seq(_))

  def variables: Parser[Seq[String]] =
    "<" ~> typeVariables <~ ">" |
      "" ^^^ Seq.empty

  def receiver: Parser[Option[Type]] =
    singleType <~ "." ^^ (Some(_)) | "" ^^^ None

  def signature: Parser[Signature] =
    variables ~
      receiver ~
      ("(" ~> types <~ ")") ~
      ("->" ~> singleType) ~
      whereClause ^^ { case typeVars ~ rcvr ~ args ~ result ~ where => Signature(rcvr, args, result, SignatureContext(typeVars.toSet, where)) }
}

object KotlinSignatureParser {

  private val kotlinSignatureParser = new KotlinSignatureParser

  def parse(str: String): Either[String, Signature] = {
    doParse(str) >>= (sgn => convert(sgn)) >>= (sgn => validate(sgn))
  }

  private def doParse(str: String): Either[String, Signature] = {
    import kotlinSignatureParser._
    kotlinSignatureParser.parse(signature, str) match {
      case Success(matched, _) => Right(matched)
      case Failure(msg, _) => Left(msg)
      case Error(msg, _) => Left(msg)
    }
  }

  private def convert(sgn: Signature): Either[String, Signature] = {
    val converter: Type => Type = resolve(sgn.context.vars)
    sgn
      .modifyAll(_.receiver.each, _.result).using(converter)
      .modify(_.arguments).using(_.map(converter))
      .modify(_.context.constraints).using(_.map(_.modify(_._2).using(_.map(converter))))
      .right[String]
  }

  private def resolve(vars: Set[String])(t: Type): Type =
    t match {
      case genType: GenericType =>
        val converter: Type => Type = resolve(vars)
        genType
          .modify(_.base).using(converter)
          .modify(_.params).using(_.map(converter))
      case _: Unresolved        =>
        vars.find(_ == t.name).fold[Type](t.asConcrete)(Function.const(t.asVariable))
      case _                    => t
    }

  private def validate(sgn: Signature): Either[String, Signature] = {
    sgn.whenOrElse(sgn.context.constraints.keySet.subsetOf(sgn.context.vars))("Constraints can only be defined for declared variables")
  }

  //TODO actually, I am not sure if THIS should be a strategy for defaults, so not used for now
  private def fallToDefault(sgn: Signature): Either[String, Signature] = {
    val default = Seq("Any".concreteType.?)
    sgn.modify(_.context.constraints).using { consts =>
      sgn.context.vars.map(v => (v, consts.getOrElse(v, default))).toMap
    }.right[String]
  }
}