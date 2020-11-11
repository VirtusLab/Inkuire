package org.virtuslab.inkuire.engine.common.parser

import com.softwaremill.quicklens._
import org.virtuslab.inkuire.engine.common.model._
import org.virtuslab.inkuire.engine.common.utils.syntax._
import cats.instances.all._
import cats.syntax.all._
import org.virtuslab.inkuire.engine.common.model.{
  ConcreteType,
  GenericType,
  Signature,
  SignatureContext,
  StarProjection,
  Type,
  TypeName,
  TypeVariable,
  UnresolvedVariance,
  Variance
}

class KotlinSignatureParser extends BaseSignatureParser {

  def concreteType: Parser[Type] =
    identifier ^^ (Unresolved(_))

  def typ: Parser[Type] =
    genericType |
      nullable(concreteType)

  def starProjection: Parser[Type] = "*" ^^^ StarProjection

  def nullable(typ: Parser[Type]): Parser[Type] =
    typ ~ nullability ^^ { case typ ~ nullable => if (nullable) typ.? else typ }

  def singleType: Parser[Type] =
    starProjection |
      functionType |
      typ |
      nullable("(" ~> singleType <~ ")")

  def receiverType: Parser[Type] =
    nullable("(" ~> functionType <~ ")") |
      ("(" ~> functionType <~ ")") |
      starProjection |
      typ |
      nullable("(" ~> receiverType <~ ")")

  def functionType: Parser[GenericType] =
    receiver ~ ("(" ~> types <~ ")") ~ ("->" ~> singleType) ^^ {
      case rcvr ~ args ~ result => mapToGenericFunctionType(rcvr, args, result)
    }

  private def mapToGenericFunctionType(receiver: Option[Type], args: Seq[Type], result: Type): GenericType = {
    val params = receiver.fold(args :+ result)(_ +: args :+ result)
    GenericType(
      ConcreteType(s"Function${params.size - 1}"),
      params.map(UnresolvedVariance)
    )
  }

  def genericType: Parser[Type] =
    nullable(concreteType ~ ("<" ~> typeArguments <~ ">") ^^ {
      case baseType ~ types => GenericType(baseType, types.map(UnresolvedVariance))
    })

  def types: Parser[Seq[Type]] = list(singleType) | empty[List[Type]]

  def typeArguments: Parser[Seq[Type]] = list(singleType) | empty[List[Type]]

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
    (typeVariable <~ ",") ~ typeVariables ^^ {
      case typeVar ~ vars =>
        (typeVar._1 +: vars._1, vars._2.updatedWith(typeVar._1)(s => Some(s.toSeq.flatten ++ typeVar._2)))
    } |
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
            (typeVars._2.keys ++ where.keys)
              .map(k => k -> (where.get(k).toSeq.flatten ++ typeVars._2.get(k).toSeq.flatten))
              .toMap
              .filter(_._2.nonEmpty)
          )
        )
    } |
      ("(" ~> signature <~ ")")
}

class KotlinSignatureParserService extends BaseSignatureParserService {

  private val kotlinSignatureParser = new KotlinSignatureParser

  override def parse(str: String): Either[String, Signature] =
    doParse(str) >>= convert >>= validate

  private def doParse(str: String): Either[String, Signature] = {
    import kotlinSignatureParser._
    kotlinSignatureParser.parseAll(signature, str) match {
      case Success(matched, _) => Right(matched)
      case Failure(msg, _)     => Left(msg)
      case Error(msg, _)       => Left(msg)
    }
  }

  private def convert(sgn: Signature): Either[String, Signature] = {
    val converter: Type => Type = resolve(sgn.context.vars)
    sgn
      .modifyAll(_.receiver.each.typ, _.result.typ)
      .using(converter)
      .modify(_.arguments.each.typ)
      .using(converter)
      .modify(_.context.constraints.each.each)
      .using(converter)
      .right[String]
  }

  private def resolve(vars: Set[String])(t: Type): Type = {
    val converter: Type => Type = resolve(vars)
    t match {
      case genType: GenericType =>
        genType
          .modify(_.base)
          .using(converter)
          .modify(_.params.each)
          .using { x =>
            UnresolvedVariance(converter(x.typ))
          }
      case u: Unresolved =>
        vars.find(TypeName(_) == u.name).fold[Type](t.asConcrete)(Function.const(t.asVariable))
      case _ => t
    }
  }

  private def validate(sgn: Signature): Either[String, Signature] = {
    for {
      _ <- validateConstraintsForNonVariables(sgn)
    } yield sgn
  }

  private def validateConstraintsForNonVariables(sgn: Signature): Either[String, Unit] =
    Either.cond(
      sgn.context.constraints.keySet.subsetOf(sgn.context.vars),
      (),
      "Constraints can only be defined for declared variables"
    )

  private def validateTypeParamsArgs(sgn: Signature): Either[String, Unit] = {
    sgn.receiver.map(doValidateVariance).getOrElse(().right) >>
      sgn.arguments.map(doValidateVariance).foldLeft[Either[String, Unit]](().right)(_ >> _) >>
      doValidateVariance(sgn.result) >>
      sgn.context.constraints.values.toSeq.flatten
        .map(doValidateTypeParamsArgs)
        .foldLeft[Either[String, Unit]](().right)(_ >> _)
  }

  private def doValidateVariance(v: Variance): Either[String, Unit] = doValidateTypeParamsArgs(v.typ)

  private def doValidateTypeParamsArgs(t: Type): Either[String, Unit] = {
    t match {
      case GenericType(base, params) =>
        Either.cond(!base.isInstanceOf[TypeVariable], (), "Type arguments are not allowed for type parameters") >>
          doValidateTypeParamsArgs(base) >>
          params.map(x => doValidateTypeParamsArgs(x.typ)).foldLeft[Either[String, Unit]](().right)(_ >> _)
      case _ => ().right
    }
  }
}
