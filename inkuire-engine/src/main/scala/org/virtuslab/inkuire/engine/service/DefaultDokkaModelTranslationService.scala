package org.virtuslab.inkuire.engine.service


import org.virtuslab.inkuire.engine.model._
import org.virtuslab.inkuire.model._
import org.virtuslab.inkuire.engine.model.Type._

import scala.jdk.CollectionConverters._

object DefaultDokkaModelTranslationService extends DokkaModelTranslationService {
  implicit def listAsScala[T](list: java.util.List[T]): Iterable[T] = list.asScala

  private def translateProjection(p: SProjection): Type = p match {
    case _: SStar => StarProjection
    case s: SVariance => translateBound(s.getInner)
    case b: SBound => translateBound(b)
  }

  private def translateTypeVariables(f: SDFunction): SignatureContext = {
    val generics = f.getGenerics.map(p => (p.getName, p.getBounds.map(translateBound).toSeq)).toMap
    SignatureContext(
      generics.keys.toSet,
      generics
    )
  }

  private def translateBound(b: SBound): Type = {
    b match {
      case n: SNullable => translateBound(n.getInner).?
      case t: STypeConstructor => t match{
        case t if !t.getProjections.isEmpty => GenericType(getTypeName(t).concreteType, t.getProjections.map(translateProjection).toSeq)
        case t => getTypeName(t).concreteType
      }
      case o: SOtherParameter => TypeVariable(getTypeName(o))
      case default => getTypeName(default).concreteType
    }
  }

  private def getTypeName(b: SBound): String = {
    b match {
      case t: STypeConstructor => t.getDri.getClassName
      case o: SOtherParameter => o.getName
      case j: SPrimitiveJavaType => j.getName
      case _: SVoid => "void"
      case p: SPrimitiveJavaType => p.getName
      case _: SDynamic => "dynamic"
      case _: SJavaObject => "Object"
    }
  }

  private def getReceiver(f: SDFunction): Option[Type] = {
    Option
      .when(f.getReceiver != null)(translateBound(f.getReceiver.getType))
      .orElse(
        Option
          .when(f.getDri.getClassName != null)(f.getDri.getClassName.concreteType)
      )
  }

  def translateTypeBound(s: SDRI): Type = ConcreteType(s.getOriginal)

  def translateFunction(f: SDFunction): List[ExternalSignature] = {

    val parametersCombinations = f.getAreParametersDefault.zip(f.getParameters.map(s => translateBound(s.getType)).toSeq)
      .foldLeft[List[List[Type]]](List(List.empty)) {
      case (acc, elem) => if (!elem._1) {
        acc.map(_ :+ elem._2)
      } else {
        acc.map(_ :+ elem._2) ++ acc
      }
    }

    parametersCombinations.map {
      case params =>
        ExternalSignature(
          Signature(
            getReceiver(f),
            params,
            translateBound(f.getType),
            translateTypeVariables(f)
          ),
          f.getName,
          f.getDri.getOriginal
        )
    }
  }
}
