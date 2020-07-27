package org.virtuslab.inkuire.engine.parser

import org.virtuslab.inkuire.engine.model.Type._
import com.softwaremill.quicklens._
import org.virtuslab.inkuire.engine.model._
import org.virtuslab.inkuire.engine.utils.syntax._
import cats.instances.all._
import cats.syntax.all._

class KotlinSignatureParser extends BaseSignatureParser {

  def concreteType: Parser[Type] =
    identifier ^^ (Unresolved(_))

  def typ: Parser[Type] =
    genericType |
      nullable(concreteType)

  def starProjection: Parser[Type] = "*" ^^^ StarProjection

  def nullable(typ: Parser[Type]): Parser[Type] =
    typ ~ nullability ^^ { case typ ~ nullable => if(nullable) typ.? else typ }

  def singleType: Parser[Type] =
    functionType |
      typ |
      nullable("(" ~> singleType <~ ")")

  def receiverType: Parser[Type] =
    nullable("(" ~> functionType <~ ")") |
      ("(" ~> functionType <~ ")") |
      typ |
      nullable("(" ~> receiverType <~ ")")

  def functionType: Parser[FunctionType] =
    receiver ~ ("(" ~> types <~ ")") ~ ("->" ~> singleType) ^^ { case rcvr ~ args ~ result => FunctionType(rcvr, args, result) }

  def genericType: Parser[Type] =
    nullable(concreteType ~ ("<" ~> typeArguments <~ ">") ^^ { case baseType ~ types => GenericType(baseType, types) })

  def types: Parser[Seq[Type]] = list(singleType) | empty[List[Type]]

  def typeArgument: Parser[Type] = singleType | starProjection

  def typeArguments: Parser[Seq[Type]] = list(typeArgument) | empty[List[Type]]

  def constraint: Parser[(String, Type)] =
    (identifier <~ ":") ~ singleType ^^ { case id ~ typ => (id, typ) }

  def constraints: Parser[Seq[(String, Type)]] = list(constraint)

  def whereClause: Parser[Map[String, Seq[Type]]] =
    "where" ~> constraints ^^ (_.groupBy(_._1).map { case (k, v) => (k, v.map(_._2)) }) |
      "" ^^^ Map.empty

  def typeVariable: Parser[(String, Seq[Type])] =
    (identifier <~ ":") ~ singleType ^^ { case typeVar ~ constraint => (typeVar, Seq(constraint)) } |
    identifier ^^ ((_, Seq.empty[Type]))

  def typeVariables: Parser[(Seq[String], Map[String, Seq[Type]])] =
    (typeVariable <~ "," ) ~ typeVariables ^^ { case typeVar ~ vars => (typeVar._1 +: vars._1, vars._2.updatedWith(typeVar._1)(s => Some(s.toSeq.flatten ++ typeVar._2))) } |
      typeVariable ^^ (v => (Seq(v._1), Map(v._1 -> v._2)))

  def variables: Parser[(Seq[String], Map[String, Seq[Type]])] =
    "<" ~> typeVariables <~ ">" |
      "" ^^^ (Seq.empty, Map.empty)

  def receiver: Parser[Option[Type]] =
    receiverType <~ "." ^^ (Some(_)) | "" ^^^ None

  def signature: Parser[Signature] =
    variables ~
      receiver ~
      ("(" ~> types <~ ")") ~
      ("->" ~> singleType) ~
      whereClause ^^ {
        case typeVars ~ rcvr ~ args ~ result ~ where =>
          Signature(
            rcvr,
            args,
            result,
            SignatureContext(
              typeVars._1.toSet,
              (typeVars._2.keys ++ where.keys).map(k => k -> (where.get(k).toSeq.flatten ++ typeVars._2.get(k).toSeq.flatten)).toMap.filter(_._2.nonEmpty)
            )
          )
      } |
      ("(" ~> signature <~ ")")
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
      .modify(_.arguments.each).using(converter)
      .modify(_.context.constraints.each.each).using(converter)
      .right[String]
  }

  private def resolve(vars: Set[String])(t: Type): Type = {
    val converter: Type => Type = resolve(vars)
    t match {
      case genType: GenericType  =>
        genType
          .modify(_.base).using(converter)
          .modify(_.params.each).using(converter)
      case funType: FunctionType =>
        funType
          .modify(_.receiver.each).using(converter)
          .modify(_.args.each).using(converter)
          .modify(_.result).using(converter)
      case u: Unresolved         =>
        vars.find(_ == u.name).fold[Type](t.asConcrete)(Function.const(t.asVariable))
      case _                     => t
    }
  }

  private def validate(sgn: Signature): Either[String, Signature] = {
    for {
      _ <- validateConstraintsForNonVariables(sgn)
      _ <- validateTypeParamsArgs(sgn)
      _ <- validateUpperBounds(sgn)
    } yield sgn
  }

  private def validateConstraintsForNonVariables(sgn: Signature): Either[String, Unit] =
    Either.cond(sgn.context.constraints.keySet.subsetOf(sgn.context.vars), (), "Constraints can only be defined for declared variables")

  private def validateTypeParamsArgs(sgn: Signature): Either[String, Unit] = {
    sgn.receiver.map(doValidateTypeParamsArgs).getOrElse(().right) >>
      sgn.arguments.map(doValidateTypeParamsArgs).foldLeft[Either[String, Unit]](().right)(_ >> _) >>
      doValidateTypeParamsArgs(sgn.result) >>
      sgn.context.constraints.values.toSeq.flatten.map(doValidateTypeParamsArgs).foldLeft[Either[String, Unit]](().right)(_ >> _)
  }

  private def doValidateTypeParamsArgs(t: Type): Either[String, Unit] = {
    t match {
      case GenericType(base, params)               =>
        Either.cond(!base.isInstanceOf[TypeVariable], (), "Type arguments are not allowed for type parameters") >>
          doValidateTypeParamsArgs(base) >>
          params.map(doValidateTypeParamsArgs).foldLeft[Either[String, Unit]](().right)(_ >> _)
      case FunctionType(receiver, args, result, _) =>
        receiver.map(doValidateTypeParamsArgs).getOrElse(().right) >>
          args.map(doValidateTypeParamsArgs).foldLeft[Either[String, Unit]](().right)(_ >> _) >>
          doValidateTypeParamsArgs(result)
      case _                                       => ().right
    }
  }

  private def validateUpperBounds(sgn: Signature): Either[String, Unit] = {
    Either.cond(
      sgn.context.constraints.values.toSeq.flatten.collect {
        case t: FunctionType => t.receiver.isEmpty
      }.forall(identity),
      (),
      "Extension function cannot be used as upper bound"
    )
  }

  //TODO actually, I am not sure if THIS should be a strategy for defaults, so not used for now
  private def fallToDefault(sgn: Signature): Either[String, Signature] = {
    val default = Seq("Any".concreteType.?)
    sgn.modify(_.context.constraints).using { consts =>
      sgn.context.vars.map(v => (v, consts.getOrElse(v, default))).toMap
    }.right[String]
  }
}