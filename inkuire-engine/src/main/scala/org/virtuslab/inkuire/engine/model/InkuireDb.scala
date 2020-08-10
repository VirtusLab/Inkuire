package org.virtuslab.inkuire.engine.model

import java.io.{File, FileReader}

import com.google.gson.{JsonIOException, JsonSyntaxException}
import com.google.gson.reflect.TypeToken
import org.virtuslab.inkuire.model._
import org.virtuslab.inkuire.engine.model.Type._
import org.virtuslab.inkuire.engine.service.{DefaultDokkaModelTranslationService, DokkaModelTranslationService}
import org.virtuslab.inkuire.model.util.CustomGson

import scala.jdk.CollectionConverters._
import scala.annotation.tailrec

case class InkuireDb(
  functions: Seq[ExternalSignature],
  types: Map[Type, Set[Type]]
)

object InkuireDb {

  val translationService: DokkaModelTranslationService = DefaultDokkaModelTranslationService

  def read(functionFiles: List[File], ancestryFiles: List[File]): Either[String, InkuireDb] = {
    try {
      val functions = functionFiles.flatMap { file =>
        CustomGson.INSTANCE.getWithSDocumentableAdapters.fromJson(
          new FileReader(file),
          new TypeToken[Array[SDFunction]] {}.getType
        ).asInstanceOf[Array[SDFunction]].toList
      }.map(translationService.translateFunction)

      val ancestryGraph = ancestryFiles.flatMap { file =>
        CustomGson.INSTANCE.getWithAncestryGraphAdapters.fromJson(
          new FileReader(file),
          new TypeToken[java.util.Map[SDRI, Array[SDRI]]] {}.getType
        ).asInstanceOf[java.util.Map[SDRI, Array[SDRI]]].asScala.map { case (sdri, sdris) => sdri -> sdris.toList }
      }.map {
        case (sdri, listOfSdris) =>
          translationService.translateTypeBound(sdri) -> listOfSdris.map(translationService.translateTypeBound).toSet
      }.toMap

      Right(new InkuireDb(functions, ancestryGraph))
    } catch {
      case m: JsonSyntaxException => Left(m.getMessage)
      case m: JsonIOException => Left(m.getMessage)
    }
  }

}

case class ExternalSignature(
  signature: Signature,
  name: String,
  uri: String
)