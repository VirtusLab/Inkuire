package org.virtuslab.inkuire.engine.common.model

import cats.implicits.{catsSyntaxOptionId, toBifunctorOps, toShow, toTraverseOps}
import org.virtuslab.inkuire.engine.common.service.DefaultDokkaModelTranslationService.translateDRI
import io.circe._
import io.circe.parser._
import io.circe.generic.auto._
import io.circe.syntax._
import org.virtuslab.inkuire.engine.common.service.{DefaultDokkaModelTranslationService, DokkaModelTranslationService}
import org.virtuslab.inkuire.model._

case class InkuireDb(
  functions: Seq[ExternalSignature],
  types:     Map[ITID, (Type, Seq[Type])]
)

object InkuireDb {

  val translationService: DokkaModelTranslationService = DefaultDokkaModelTranslationService

  def read(functionDbs: List[String], ancestryDbs: List[String]): Either[String, InkuireDb] = {
    val res = for {
      ancestryGraph <- ancestryFilesToTypes(ancestryDbs).map(_.filterOutImmediateCycles)

      functions <- functionFilesToExternalSignatures(functionDbs, ancestryGraph)
        .map(_.populateVariances(ancestryGraph))
        .map(_.remapVariableDRIs)
    } yield new InkuireDb(functions, ancestryGraph)
    res.leftMap(e => e.show)
  }

  private def functionFilesToExternalSignatures(
    functionDbs:   List[String],
    ancestryGraph: Map[ITID, (Type, Seq[Type])]
  ): Either[Error, Seq[ExternalSignature]] =
    functionDbs
      .traverse { content =>
        import org.virtuslab.inkuire.model.util.Deserializer._
        parse(content).flatMap(_.as[Seq[SDFunction]])
      }
      .map(_.flatten.flatMap(translationService.translateFunction(_, ancestryGraph)))

  private def ancestryFilesToTypes(ancestryDbs: List[String]): Either[Error, Map[ITID, (Type, Seq[Type])]] =
    ancestryDbs
      .traverse { content =>
        import org.virtuslab.inkuire.model.util.Deserializer._
        parse(content).flatMap(_.as[Seq[AncestryGraph]])
      }
      .map(_.flatten.map { a =>
        translateDRI(a.dri) -> (translationService.translateProjection(a.`type`) -> a.superTypes
          .map(translationService.translateProjection))
      }.toMap)

  def mapTypesParametersVariance(types: Map[ITID, (Type, Seq[Type])]): PartialFunction[Type, Type] = {
    case typ: GenericType => mapGenericTypesParametersVariance(typ, types)
    case typ => typ
  }

  implicit class AncestryGraphOps(receiver: Map[ITID, (Type, Seq[Type])]) {

    def filterOutImmediateCycles: Map[ITID, (Type, Seq[Type])] = {
      receiver.map {
        case (dri, (typ, parents)) =>
          dri -> (typ, parents.filter(_.itid != dri.some))
      }
    }

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

  implicit class FunctionsOps(receiver: Seq[ExternalSignature]) {
    import com.softwaremill.quicklens._

    def populateVariances(types: Map[ITID, (Type, Seq[Type])]): Seq[ExternalSignature] =
      receiver.map {
        case ExternalSignature(Signature(receiver, arguments, result, context), name, packageName, uri) =>
          import com.softwaremill.quicklens._
          val rcv  = receiver.map(r => r.modify(_.typ).using(mapTypesParametersVariance(types)))
          val args = arguments.map(a => a.modify(_.typ).using(mapTypesParametersVariance(types)))
          val rst = result.modify(_.typ).using {
            case typ: GenericType => mapGenericTypesParametersVariance(typ, types)
            case typ => typ
          }
          ExternalSignature(Signature(rcv, args, rst, context), name, packageName, uri)
      }

    def remapVariableDRIs: Seq[ExternalSignature] =
      receiver.map(remapFunctionVariableDRIs)

    private def remapFunctionVariableDRIs(externalSignature: ExternalSignature): ExternalSignature = {
      externalSignature
        .modify(_.signature)
        .using { s =>
          s.modifyAll(_.receiver.each.typ, _.arguments.each.typ, _.result.typ)
            .using(remapTypeVariableDRIs)
            .modify(_.context.constraints)
            .using { constraints =>
              constraints.toList.map(_.modify(_._2.each).using(remapTypeVariableDRIs)).toMap
            }
        }
    }

    private final val externalVariableDRI = "external-iri-"

    private def remapTypeVariableDRIs: Type => Type = {
      case t: TypeVariable =>
        t.modify(_.itid.each).setTo(ITID(externalVariableDRI + t.name.name, isParsed = false))
      case g: GenericType =>
        g.modifyAll(_.base, _.params.each.typ).using(remapTypeVariableDRIs)
      case t => t
    }
  }

  private def mapGenericTypesParametersVariance(typ: GenericType, types: Map[ITID, (Type, Seq[Type])]): GenericType = {
    if (types.contains(typ.base.itid.get)) {
      GenericType(
        typ.base,
        types(typ.base.itid.get)._1.params.zip(typ.params).map {
          case (variance, irrelevantVariance) => wrapWithVariance(irrelevantVariance.typ, variance)
        }
      )
    } else typ
  }

  private def wrapWithVariance(typ: Type, variance: Variance) =
    variance match {
      case _: Covariance         => Covariance(typ)
      case _: Contravariance     => Contravariance(typ)
      case _: Invariance         => Invariance(typ)
      case _: UnresolvedVariance => UnresolvedVariance(typ)
    }
}

case class ExternalSignature(
  signature:   Signature,
  name:        String,
  packageName: String,
  uri:         String
)
