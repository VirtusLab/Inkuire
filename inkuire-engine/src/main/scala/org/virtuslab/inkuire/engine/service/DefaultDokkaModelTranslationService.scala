package org.virtuslab.inkuire.engine.service

import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.model._
import org.virtuslab.inkuire.model.{SNullable, SPrimitiveJavaType, STypeConstructor, STypeParameter, SVariance, _}
import org.virtuslab.inkuire.engine.model.Type._

import scala.jdk.CollectionConverters._

object DefaultDokkaModelTranslationService extends DokkaModelTranslationService {
  implicit def listAsScala[T](list: java.util.List[T]): Iterable[T] = list.asScala

  private def translateProjection(p: SProjection): Type = p match {
    case _: SStar     => StarProjection
    case s: SVariance => translateBound(s.getInner)
    case b: SBound    => translateBound(b)
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
      case t: STypeConstructor =>
        val core = ConcreteType(t.getDri.getClassName, dri = translateDRI(t.getDri).some)
        if (t.getProjections.isEmpty) core else GenericType(core, t.getProjections.map(translateProjection).toSeq)
      case t: STypeParameter => TypeVariable(getTypeName(t), dri = translateDRI(t.getDri).some)
      case _ => getTypeName(b).concreteType
    }
  }

  private def getTypeName: SBound => String = {
    case t: STypeConstructor   => t.getDri.getClassName
    case o: STypeParameter     => o.getName
    case j: SPrimitiveJavaType => j.getName
    case _: SVoid              => "void"
    case _: SDynamic           => "dynamic"
    case _: SJavaObject        => "Object"
    case _ => "Any" //TODO why is this needed?
  }

  private def getReceiver(f: SDFunction): Option[Type] = {
    Option
      .when(f.getReceiver != null)(translateBound(f.getReceiver.getType))
      .orElse(
        Option
          .when(f.getDri.getClassName != null) {
            ConcreteType(
              f.getDri.getClassName,
              dri = translateDRI(f.getDri).copy(
                callableName = None,
                original = f.getDri.getOriginal.split("/").patch(2, "", 1).mkString(sep = "/")
              ).some
            )
          }
      )
  }

  def translateTypeBound(projection: SProjection): Type = translateBound(projection.asInstanceOf[SBound])

  def translateDRI(sdri: SDRI): DRI = DRI(
    if (sdri.getPackageName != null) sdri.getPackageName.some else None,
    if (sdri.getClassName != null) sdri.getClassName.some else None,
    if (sdri.getCallableName != null) sdri.getCallableName.some else None,
    sdri.getOriginal
  )

  def translateFunction(f: SDFunction): List[ExternalSignature] = {

    val parametersCombinations = f.getAreParametersDefault
      .zip(f.getParameters.map(s => translateBound(s.getType)).toSeq)
      .foldLeft[List[List[Type]]](List(List.empty)) { (acc, elem) =>
        if (!elem._1) {
          acc.map(_ :+ elem._2)
        } else {
          acc.map(_ :+ elem._2) ++ acc
        }
      }

    parametersCombinations.map { params =>
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
