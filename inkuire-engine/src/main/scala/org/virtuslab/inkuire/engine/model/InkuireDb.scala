package org.virtuslab.inkuire.engine.model

import java.io.{File, FileReader}

import com.google.gson.reflect.TypeToken
import org.virtuslab.inkuire.model._
import org.virtuslab.inkuire.engine.model.Type._
import org.virtuslab.inkuire.model.util.CustomGson

import scala.jdk.CollectionConverters._
import scala.annotation.tailrec

case class InkuireDb(
  functions: Seq[ExternalSignature],
  types: Map[Type, Set[Type]]
)

object InkuireDb {
  implicit def listAsScala[T](list: java.util.List[T]): Iterable[T] = list.asScala

  @tailrec
  private def parseBound(b: SBound): String = {
    b match {
      case t: STypeConstructor => t.getDri.getClassName
      case o: SOtherParameter => o.getName
      case n: SNullable => parseBound(n.getInner)
      case j: SPrimitiveJavaType => j.getName
      case _: SVoid => "void"
      case p: SPrimitiveJavaType => p.getName
      case _: SDynamic => "dynamic"
      case _: SJavaObject => "Object"
    }
  }

  private def receiver(f: SDFunction) = {
    val receiver = f.getReceiver
    val className = f.getDri.getClassName
    if (receiver == null) {
      if (className == null) None
      else Some(className.concreteType)
    } else Some(receiver.getName.concreteType)
  }

  private def generateFunctionSignature(f: SDFunction) = ExternalSignature(
    Signature(
      receiver(f),
      f.getParameters.map(s => s.getName.concreteType).toSeq,
      parseBound(f.getType).concreteType,
      SignatureContext.empty
    ),
    f.getName,
    f.getDri.getOriginal
  )

  private def generateTypeBound(s: SDRI): Type = ConcreteType(s.getOriginal)

  def read(functionFiles: List[File], ancestryFiles: List[File]): InkuireDb = {

    val functions = functionFiles.flatMap { file =>
      CustomGson.INSTANCE.getWithSDocumentableAdapters.fromJson(
        new FileReader(file),
        new TypeToken[Array[SDFunction]] {}.getType
      ).asInstanceOf[Array[SDFunction]].toList
    }.map(generateFunctionSignature)

    val ancestryGraph = ancestryFiles.flatMap { file =>
      CustomGson.INSTANCE.getWithAncestryGraphAdapters.fromJson(
        new FileReader(file),
        new TypeToken[java.util.Map[SDRI, Array[SDRI]]] {}.getType
      ).asInstanceOf[java.util.Map[SDRI, Array[SDRI]]].asScala.map { case (sdri, sdris) => sdri -> sdris.toList }
    }.map {
      case (sdri, listOfSdris) => generateTypeBound(sdri) -> listOfSdris.map(generateTypeBound).toSet
    }.toMap

    new InkuireDb(functions, ancestryGraph)
  }
}

case class ExternalSignature(
  signature: Signature,
  name: String,
  uri: String
)