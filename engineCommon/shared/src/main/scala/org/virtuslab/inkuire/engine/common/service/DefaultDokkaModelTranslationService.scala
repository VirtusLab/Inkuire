package org.virtuslab.inkuire.engine.common.service

import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.common.model._
import org.virtuslab.inkuire.model.{SNullable, SPrimitiveJavaType, STypeConstructor, STypeParameter, SVariance, _}
import Type._

import scala.jdk.CollectionConverters._

object DefaultDokkaModelTranslationService extends DokkaModelTranslationService {
  implicit def listAsScala[T](list: java.util.List[T]): Iterable[T] = list.asScala

  private def translateTypeVariables(f: SDFunction): SignatureContext = {
    val generics = f.generics
      .map(p =>
        (
          p.variantTypeParameter.inner.asInstanceOf[STypeParameter].name,
          p.bounds.map(translateBound).toSeq
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
      case n: SNullable => translateBound(n.inner).?
      case t: STypeConstructor =>
        val core = ConcreteType(TypeName(t.dri.className.get), dri = translateDRI(t.dri).some)
        if (t.projections.isEmpty) core
        else GenericType(core, t.projections.map(translateProjectionVariance).toSeq)
      case t: STypeParameter => TypeVariable(getTypeName(t), dri = translateDRI(t.dri).some)
      case _ => getTypeName(b).concreteType
    }
  }

  private def getTypeName: PartialFunction[SBound, String] = {
    case t: STypeConstructor   => t.dri.className.getOrElse("")
    case o: STypeParameter     => o.name
    case j: SPrimitiveJavaType => j.name
    case _: SVoid.type         => "void"
    case _: SDynamic.type      => "dynamic"
    case _: SJavaObject.type   => "Object"
    case _ => "Any" //TODO why is this needed?
  }

  private def getReceiver(f: SDFunction, ancestryGraph: Map[DRI, (Type, Seq[Type])]): Option[Type] = {
    f.receiver
      .map(receiver => translateBound(receiver.`type`))
      .orElse {
        ancestryGraph
          .get(
            translateDRI(f.dri).copy(
              callableName = None,
              original = s"${f.dri.packageName.getOrElse("")}/${f.dri.className.getOrElse("")}///PointingToDeclaration/"
            )
          )
          .map(_._1)
      }
  }

  private def translateProjectionVariance(projection: SProjection): Variance =
    projection match {
      case s: SVariance =>
        translateVariance(s)(translateBound(s.inner))
      case b: SBound     => Invariance(translateBound(b))
      case _: SStar.type => Invariance(StarProjection)
    }

  private def translateVariance(variance: SVariance): Type => Variance = {
    variance match {
      case _: SContravariance => Contravariance.apply
      case _: SCovariance     => Covariance.apply
      case _: SInvariance     => Invariance.apply
    }
  }

  def translateProjection(projection: SProjection): Type =
    projection match {
      case _: SStar.type => StarProjection
      case s: SVariance  => translateBound(s.inner)
      case b: SBound     => translateBound(b)
    }

  def translateDRI(sdri: SDRI): DRI =
    DRI(
      sdri.packageName,
      sdri.className,
      sdri.callableName,
      sdri.original
    )

  def translateFunction(f: SDFunction, ancestryGraph: Map[DRI, (Type, Seq[Type])]): List[ExternalSignature] = {

    val parametersCombinations = f.areParametersDefault
      .zip(f.parameters.map(s => translateBound(s.`type`)).toSeq)
      .foldLeft[List[List[Type]]](List(List.empty)) { (acc, elem) =>
        if (!elem._1) {
          acc.map(_ :+ elem._2)
        } else {
          acc.map(_ :+ elem._2) ++ acc
        }
      }
    val dri = translateDRI(f.dri)

    parametersCombinations.map { params =>
      ExternalSignature(
        Signature(
          getReceiver(f, ancestryGraph),
          params,
          translateBound(f.`type`),
          translateTypeVariables(f)
        ),
        f.name,
        f.location
      )
    }
  }

}
