package org.virtuslab.inkuire.engine.model

import java.io.{BufferedReader, File, FileReader, InputStreamReader}
import java.net.URL

import com.google.gson.{JsonIOException, JsonSyntaxException}
import com.google.gson.reflect.TypeToken
import org.virtuslab.inkuire.engine.service.DefaultDokkaModelTranslationService.translateDRI
import org.virtuslab.inkuire.model._
import org.virtuslab.inkuire.engine.service.{
  DefaultDokkaModelTranslationService,
  DokkaModelTranslationService,
  KotlinExternalSignaturePrettifier
}
import org.virtuslab.inkuire.model.util.CustomGson

import scala.jdk.CollectionConverters.CollectionHasAsScala
import com.softwaremill.quicklens._

case class InkuireDb(
  functions: Seq[ExternalSignature],
  types:     Map[DRI, (Type, Seq[Type])]
)

case class DRI( // This mirror class of SDRI is done on purpose, to eliminate any future inconveniences when accessing Kotlin code from Scala
  packageName:  Option[String],
  className:    Option[String],
  callableName: Option[String],
  original:     String
) {
  override def toString(): String = original
}

object InkuireDb {

  val translationService: DokkaModelTranslationService = DefaultDokkaModelTranslationService

  def read(functionFiles: List[URL], ancestryFiles: List[URL]): Either[String, InkuireDb] = {
    try {

      val ancestryGraph = ancestryFilesToTypes(ancestryFiles)

      val functions = functionFilesToExternalSignatures(functionFiles)
        .populateVariances(ancestryGraph)

      Right(new InkuireDb(functions, ancestryGraph))
    } catch {
      case m: JsonSyntaxException => Left(m.getMessage)
      case m: JsonIOException     => Left(m.getMessage)
    }
  }

  private def functionFilesToExternalSignatures(functionFiles: List[URL]): Seq[ExternalSignature] =
    functionFiles
      .flatMap { file =>
        CustomGson.INSTANCE.getInstance
          .fromJson(
            new BufferedReader(new InputStreamReader(file.openStream())),
            new TypeToken[Array[SDFunction]] {}.getType
          )
          .asInstanceOf[Array[SDFunction]]
          .toList
      }
      .flatMap(translationService.translateFunction)

  private def ancestryFilesToTypes(ancestryFiles: List[URL]): Map[DRI, (Type, Seq[Type])] =
    ancestryFiles
      .flatMap { file =>
        CustomGson.INSTANCE.getInstance
          .fromJson(
            new BufferedReader(new InputStreamReader(file.openStream())),
            new TypeToken[Array[AncestryGraph]] {}.getType
          )
          .asInstanceOf[Array[AncestryGraph]]
      }
      .map { x: AncestryGraph =>
        translateDRI(x.getDri) -> (translationService.translateProjection(x.getType) -> x.getSuperTypes.asScala.toList
          .map(translationService.translateProjection))
      }
      .toMap

  def mapTypesParametersVariance(types: Map[DRI, (Type, Seq[Type])]): PartialFunction[Type, Type] = {
    case typ: GenericType => mapGenericTypesParametersVariance(typ, types)
    case typ => typ
  }

  implicit class AncestryGraphOps(val receiver: Map[DRI, (Type, Seq[Type])]) {

    // TODO: Reconsider whether we need actually want to do this, and when
//    def populateVariances: Map[DRI, (Type, Seq[Type])] = {
//      receiver.map {
//        case (dri, (typ, ancestors)) => {
//          val mappedAncestors = ancestors.map(mapTypesParametersVariance(receiver))
//          (dri, (typ, mappedAncestors))
//        }
//      }
//    }
  }

  implicit class FunctionsOps(val receiver: Seq[ExternalSignature]) {

    def populateVariances(types: Map[DRI, (Type, Seq[Type])]): Seq[ExternalSignature] =
      receiver.map {
        case ExternalSignature(Signature(receiver, arguments, result, context), name, uri) =>
          import com.softwaremill.quicklens._
          val rcv  = receiver.map(r  => r.modify(_.typ).using(mapTypesParametersVariance(types)))
          val args = arguments.map(a => a.modify(_.typ).using(mapTypesParametersVariance(types)))
          val rst = result.modify(_.typ).using {
            case typ: GenericType => mapGenericTypesParametersVariance(typ, types)
            case typ => typ
          }
          ExternalSignature(Signature(rcv, args, rst, context), name, uri)
      }
  }

  private def mapGenericTypesParametersVariance(typ: GenericType, types: Map[DRI, (Type, Seq[Type])]): GenericType = {
    if (types.contains(typ.base.dri.get)) {
      GenericType(typ.base, types(typ.base.dri.get)._1.params.zip(typ.params).map {
        case (variance, irrelevantVariance) => wrapWithVariance(irrelevantVariance.typ, variance)
      })
    } else typ
  }

  private def wrapWithVariance(typ: Type, variance: Variance) = variance match {
    case _: Covariance         => Covariance(typ)
    case _: Contravariance     => Contravariance(typ)
    case _: Invariance         => Invariance(typ)
    case _: UnresolvedVariance => UnresolvedVariance(typ)
  }
}

case class ExternalSignature(
  signature: Signature,
  name:      String,
  uri:       String
)
