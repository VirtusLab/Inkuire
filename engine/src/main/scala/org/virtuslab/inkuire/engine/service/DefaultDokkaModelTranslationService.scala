package org.virtuslab.inkuire.engine.service

import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.model._
import org.virtuslab.inkuire.model.{SNullable, SPrimitiveJavaType, STypeConstructor, STypeParameter, SVariance, _}
import org.virtuslab.inkuire.engine.model.Type._

import scala.jdk.CollectionConverters._

object DefaultDokkaModelTranslationService extends DokkaModelTranslationService {
  implicit def listAsScala[T](list: java.util.List[T]): Iterable[T] = list.asScala

  private def translateTypeVariables(f: SDFunction): SignatureContext = {
    val generics = f.getGenerics
      .map(
        p =>
          (
            p.getVariantTypeParameter.getInner.asInstanceOf[STypeParameter].getName,
            p.getBounds.map(translateBound).toSeq
        )
      )
      .toMap
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
        if (t.getProjections.isEmpty) core
        else GenericType(core, t.getProjections.map(translateProjectionVariance).toSeq)
      case t: STypeParameter => TypeVariable(getTypeName(t), dri = translateDRI(t.getDri).some)
      case _ => getTypeName(b).concreteType
    }
  }

  private def getTypeName: PartialFunction[SBound, String] = {
    case t: STypeConstructor   => t.getDri.getClassName
    case o: STypeParameter     => o.getName
    case j: SPrimitiveJavaType => j.getName
    case _: SVoid              => "void"
    case _: SDynamic           => "dynamic"
    case _: SJavaObject        => "Object"
    case _ => "Any" //TODO why is this needed?
  }

  private def getReceiver(f: SDFunction, ancestryGraph: Map[DRI, (Type, Seq[Type])]): Option[Type] = {
    Option(f.getReceiver)
      .map(receiver => translateBound(receiver.getType))
      .orElse {
        ancestryGraph
          .get(
            translateDRI(f.getDri).copy(
              callableName = None,
              original     = s"${f.getDri.getPackageName}/${f.getDri.getClassName}///PointingToDeclaration/"
            )
          )
          .map(_._1)
      }
  }

  private def translateProjectionVariance(projection: SProjection): Variance = projection match {
    case s: SVariance[_] =>
      translateVariance(s)(translateBound(s.getInner))
    case b: SBound => Invariance(translateBound(b))
    case _: SStar  => Invariance(StarProjection)
  }

  private def translateVariance(variance: SVariance[_]): Type => Variance = {
    variance match {
      case _: SContravariance[_] => Contravariance.apply
      case _: SCovariance[_]     => Covariance.apply
      case _: SInvariance[_]     => Invariance.apply
    }
  }

  def translateProjection(projection: SProjection): Type = projection match {
    case _: SStar        => StarProjection
    case s: SVariance[_] => translateBound(s.getInner)
    case b: SBound       => translateBound(b)
  }

  def translateDRI(sdri: SDRI): DRI = DRI(
    if (sdri.getPackageName != null) sdri.getPackageName.some else None,
    if (sdri.getClassName != null) sdri.getClassName.some else None,
    if (sdri.getCallableName != null) sdri.getCallableName.some else None,
    sdri.getOriginal
  )

  def translateFunction(f: SDFunction, ancestryGraph: Map[DRI, (Type, Seq[Type])]): List[ExternalSignature] = {

    val parametersCombinations = f.getAreParametersDefault
      .zip(f.getParameters.map(s => translateBound(s.getType)).toSeq)
      .foldLeft[List[List[Type]]](List(List.empty)) { (acc, elem) =>
        if (!elem._1) {
          acc.map(_ :+ elem._2)
        } else {
          acc.map(_ :+ elem._2) ++ acc
        }
      }
    val dri = translateDRI(f.getDri)

    parametersCombinations.map { params =>
      ExternalSignature(
        Signature(
          getReceiver(f, ancestryGraph),
          params,
          translateBound(f.getType),
          translateTypeVariables(f)
        ),
        f.getName,
        Seq(dri.packageName, dri.className).flatten.mkString(".")
      )
    }
  }

}
