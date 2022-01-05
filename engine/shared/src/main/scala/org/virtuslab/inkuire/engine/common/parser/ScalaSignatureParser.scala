package org.virtuslab.inkuire.engine.common.parser

import cats.instances.all._
import cats.syntax.all._
import com.softwaremill.quicklens._
import org.virtuslab.inkuire.engine.common.api._
import org.virtuslab.inkuire.engine.common.model._

import scala.util.matching.Regex

class ScalaSignatureParser extends BaseSignatureParser {

  def concreteType: Parser[Type] =
    identifier ^^ (Type(_, isUnresolved = true)) |
      "`" ~> identifier <~ "`" ^^ (Type(_, isUnresolved = false))

  def typ: Parser[Type] =
    genericType |
      concreteType

  def starProjection: Parser[Type] = "_" ^^^ Type.StarProjection

  def singleType: Parser[Type] =
    starProjection |
      functionType |
      tupleType |
      typ

  def typeLike: Parser[TypeLike] =
    singleType |
      orType |
      andType

  def orType: Parser[OrType] =
    "(" ~> typeLike ~ ("|" ~> typeLike) <~ ")" ^^ { case t1 ~ t2 => OrType(t1, t2) }

  def andType: Parser[AndType] =
    "(" ~> typeLike ~ ("&" ~> typeLike) <~ ")" ^^ { case t1 ~ t2 => AndType(t1, t2) }

  def functionType: Parser[Type] =
    "(" ~> curriedFunctionTypes <~ ")" ^^ {
      case types => mapToGenericFunctionType(None, types.init, types.last)
    }

  def tupleTypes: Parser[Seq[TypeLike]] =
    (typeLike <~ ",") ~ tupleTypes ^^ { case t1 ~ ts => t1 +: ts } |
      (typeLike <~ ",") ~ typeLike ^^ { case t1 ~ t2 => List(t1, t2) }

  def tupleType: Parser[Type] =
    "(" ~> tupleTypes <~ ")" ^^ { case types => mapToTupleType(types) }

  def curriedTypes: Parser[Seq[TypeLike]] =
    (typeLike <~ "=>") ~ curriedTypes ^^ { case t1 ~ ts => t1 +: ts } |
      (typeLike <~ "=>") ~ typeLike ^^ { case t1 ~ t2 => List(t1, t2) }

  def curriedFunctionTypes: Parser[Seq[TypeLike]] =
    "=>" ~> typeLike ^^ { case t => List(t) } |
      curriedTypes

  private def mapToGenericFunctionType(receiver: Option[Type], args: Seq[TypeLike], result: TypeLike): Type = {
    val params = receiver.fold(args :+ result)(_ +: args :+ result)
    Type(
      s"Function${params.size - 1}",
      params = params.map(UnresolvedVariance)
    )
  }

  private def mapToTupleType(args: Seq[TypeLike]): Type =
    Type(
      s"Tuple${args.size}",
      params = args.map(UnresolvedVariance)
    )

  def genericType: Parser[Type] =
    identifier ~ ("[" ~> typeArguments <~ "]") ^^ {
      case baseType ~ types => Type(baseType, types.map(UnresolvedVariance))
    }

  def types: Parser[Seq[TypeLike]] = list(typeLike) | empty[List[TypeLike]]

  def typeArguments: Parser[Seq[TypeLike]] = list(typeLike) | empty[List[TypeLike]]

  def typeVariable: Parser[(String, Seq[Type])] =
    (identifier <~ "<:") ~ singleType ^^ { case typeVar ~ constraint => (typeVar, Seq(constraint)) } |
      identifier ^^ ((_, Seq.empty[Type]))

  def typeVariables: Parser[(Seq[String], Map[String, Seq[Type]])] =
    (typeVariable <~ ",") ~ typeVariables ^^ {
      case typeVar ~ vars =>
        (typeVar._1 +: vars._1, vars._2.updatedWith(typeVar._1)(s => Some(s.toSeq.flatten ++ typeVar._2)))
    } |
      typeVariable ^^ (v => (Seq(v._1), Map(v._1 -> v._2)))

  //TODO change to upper and lower bounds when the model can work with it
  def curriedVariables: Parser[(Seq[String], Map[String, Seq[Type]])] =
    "[" ~> typeVariables <~ "]" <~ "=>" |
      "" ^^^ (Seq.empty, Map.empty)

  def curriedSignature: Parser[Signature] =
    curriedVariables ~ curriedFunctionTypes ^^ {
      case vars ~ types =>
        mapToSignature(None, types.dropRight(1), types.last, vars, Map.empty)
    }

  def packageIdentifier: Parser[String] = """[A-Za-z][a-zA-Z0-9_.]*""".r

  def includeSignatureFilters: Parser[IncludeSignatureFilters] =
    rep1("+" ~> packageIdentifier) ^^ IncludeSignatureFilters.apply

  def excludeSignatureFilters: Parser[ExcludeSignatureFilters] =
    rep("-" ~> packageIdentifier) ^^ ExcludeSignatureFilters.apply

  def signatureFilters: Parser[SignatureFilters] =
    includeSignatureFilters | excludeSignatureFilters

  def signature: Parser[ParsedSignature] =
    signatureFilters ~ curriedSignature ^^ {
      case filters ~ siangture => ParsedSignature(siangture, filters)
    }

  def mapToSignature(
    rcvr:     Option[TypeLike],
    args:     Seq[TypeLike],
    result:   TypeLike,
    typeVars: (Seq[String], Map[String, Seq[Type]]),
    where:    Map[String, Seq[Type]]
  ): Signature =
    Signature(
      rcvr,
      args,
      result,
      SignatureContext(
        typeVars._1.toSet,
        (typeVars._2.keys ++ where.keys)
          .map(k => k -> (where.get(k).toSeq.flatten ++ typeVars._2.get(k).toSeq.flatten))
          .toMap
          .filter(_._2.nonEmpty)
      )
    )
}

class ScalaSignatureParserService extends BaseSignatureParserService {

  private val scalaSignatureParser = new ScalaSignatureParser

  override def parse(str: String): Either[String, ParsedSignature] =
    doParse(str) map convert map (s => curry(s)) >>= validate

  val parsingErrorGenericMessage =
    "Could not parse provided signature. Example signature looks like this: List[Int] => (Int => Boolean) => Int"

  private def doParse(str: String): Either[String, ParsedSignature] = {
    import scalaSignatureParser._
    scalaSignatureParser.parseAll(signature, str) match {
      case Success(matched, _) => Right(matched)
      case Failure(_, _)       => Left(parseError(parsingErrorGenericMessage))
      case Error(msg, _)       => Left(msg)
    }
  }

  private def convert(pSgn: ParsedSignature): ParsedSignature = {
    val converter: TypeLike => TypeLike = resolve(pSgn.signature.context.vars)
    pSgn
      .modify(_.signature)
      .using(
        _.modifyAll(_.receiver.each.typ, _.result.typ)
          .using(converter)
          .modify(_.arguments.each.typ)
          .using(converter)
          .modify(_.context.constraints.each.each)
          .using(converter)
      )
  }

  val typeVariablePattern: Regex = """([A-Za-z][0-9]?)""".r
  def isVariableByName(t: Type): Boolean =
    t.name.name match {
      case typeVariablePattern(_) => true
      case _                      => false
    }

  private def resolve(vars: Set[String])(t: TypeLike): TypeLike = {
    val converter: TypeLike => TypeLike = resolve(vars)
    t match {
      case u: Type if u.isUnresolved =>
        if (vars.find(TypeName(_) == u.name).nonEmpty || isVariableByName(u)) {
          converter(u.asVariable)
        } else {
          converter(u.asConcrete)
        }
      case t: Type =>
        t.modify(_.params.each).using(x => UnresolvedVariance(converter(x.typ)))
      case o: AndType =>
        o.modifyAll(_.left, _.right).using(converter)
      case o: OrType =>
        o.modifyAll(_.left, _.right).using(converter)
      case t: TypeLambda =>
        t.modify(_.result).using(converter)
    }
  }

  private def curry(pSgn: ParsedSignature): ParsedSignature = {
    pSgn.signature.result.typ match {
      case t: Type if t.name.name == s"Function${t.params.size - 1}" =>
        curry(
          pSgn
            .modify(_.signature)
            .using(
              _.copy(
                arguments = pSgn.signature.arguments ++ t.params.init.map(_.typ).map(Contravariance(_)),
                result = Covariance(t.params.last.typ)
              )
            )
        )
      case _ => pSgn
    }
  }

  private def validate(pSgn: ParsedSignature): Either[String, ParsedSignature] =
    for {
      _ <- validateConstraintsForNonVariables(pSgn.signature)
    } yield pSgn

  private def validateConstraintsForNonVariables(sgn: Signature): Either[String, Unit] =
    Either.cond(
      sgn.context.constraints.keySet.subsetOf(sgn.context.vars),
      (),
      "Constraints can only be defined for declared variables"
    )
}
