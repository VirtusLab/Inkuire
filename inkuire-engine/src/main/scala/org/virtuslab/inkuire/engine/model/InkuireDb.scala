package org.virtuslab.inkuire.engine.model

import java.io.{File, FileReader}

import com.google.gson.{JsonIOException, JsonSyntaxException}
import com.google.gson.reflect.TypeToken
import org.virtuslab.inkuire.engine.service.DefaultDokkaModelTranslationService.translateDRI
import org.virtuslab.inkuire.model._
import org.virtuslab.inkuire.engine.service.{DefaultDokkaModelTranslationService, DokkaModelTranslationService}
import org.virtuslab.inkuire.model.util.CustomGson
import scala.jdk.CollectionConverters.CollectionHasAsScala

case class InkuireDb(
  functions: Seq[ExternalSignature],
  types:     Map[DRI, (Type, Seq[Type])]
)

case class DRI( // This mirror class of SDRI is done on purpose, to eliminate any future inconveniences when accessing Kotlin code from Scala
  packageName: Option[String],
  className: Option[String],
  callableName: Option[String],
  original: String
) {
  override def toString(): String = original
}

object InkuireDb {

  val translationService: DokkaModelTranslationService = DefaultDokkaModelTranslationService

  def read(functionFiles: List[File], ancestryFiles: List[File]): Either[String, InkuireDb] = {
    try {
      val functions = functionFiles
        .flatMap { file =>
          CustomGson.INSTANCE.getInstance
            .fromJson(
              new FileReader(file),
              new TypeToken[Array[SDFunction]] {}.getType
            )
            .asInstanceOf[Array[SDFunction]]
            .toList
        }
        .flatMap(translationService.translateFunction)

      case class Pair[T, R](first: T, second: R)

      val ancestryGraph = ancestryFiles
        .flatMap { file =>
          CustomGson.INSTANCE.getInstance
            .fromJson(
              new FileReader(file),
              new TypeToken[Array[AncestryGraph]] {}.getType
            )
            .asInstanceOf[Array[AncestryGraph]]
        }
        .map {
          case x: AncestryGraph =>
            translateDRI(x.getDri) -> (translationService.translateProjection(x.getType) -> x.getProjections.asScala.toList.map(translationService.translateProjection))
        }
        .toMap
      Right(new InkuireDb(functions, ancestryGraph))
    } catch {
      case m: JsonSyntaxException => Left(m.getMessage)
      case m: JsonIOException     => Left(m.getMessage)
    }
  }

}

case class ExternalSignature(
  signature: Signature,
  name:      String,
  uri:       String
)
