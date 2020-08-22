package org.virtuslab.inkuire.engine.model

import java.io.{File, FileReader}

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

      val ancestryGraph = ancestryFiles
        .flatMap { file =>
          CustomGson.INSTANCE.getInstance
            .fromJson(
              new FileReader(file),
              new TypeToken[Array[AncestryGraph]] {}.getType
            )
            .asInstanceOf[Array[AncestryGraph]]
        }
        .map { x: AncestryGraph =>
          translateDRI(x.getDri) -> (translationService.translateProjection(x.getType) -> x.getProjections.asScala.toList
            .map(translationService.translateProjection))
        }
        .toMap
      //TODO #53 Move adding implicit inheritance ancestors to inkuire-dokka-plugin
      val any = ancestryGraph.values.map(_._1).filter(_.name == TypeName("Any")).head
      val formattedAncestryGraph = ancestryGraph.toSeq
        .modify(_.each._2)
        .using {
          case (t, l) => if (l.nonEmpty || t.name.name.contains("Any")) (t, l) else (t, List(any))
        }
        .toMap

      Right(new InkuireDb(functions, formattedAncestryGraph))
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
