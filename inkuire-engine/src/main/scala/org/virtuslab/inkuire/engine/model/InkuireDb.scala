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

  private def parseProjection(p: SProjection): Type = p match {
    case _: SStar => StarProjection
    case s: SVariance => parseBound(s.getInner)
    case b: SBound => parseBound(b)
  }

  private def parseGenerics(f: SDFunction): SignatureContext = {
    val generics = f.getGenerics.map(p => (p.getName, p.getBounds.map(parseBound).toSeq)).toMap
    SignatureContext(
      generics.keys.toSet,
      generics
    )
  }

  private def parseBound(b: SBound): Type = {
    b match {
      case n: SNullable => parseBound(n.getInner).?
      case t: STypeConstructor => t match{
        case t if !t.getProjections.isEmpty => GenericType(getTypeName(t).concreteType, t.getProjections.map(parseProjection).toSeq)
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

  private def receiver(f: SDFunction): Option[Type] = {
    Option
      .when(f.getReceiver != null)(parseBound(f.getReceiver.getType))
      .orElse(
        Option
          .when(f.getDri.getClassName != null)(f.getDri.getClassName.concreteType)
      )
  }

  private def generateFunctionSignature(f: SDFunction) = ExternalSignature(
    Signature(
      receiver(f),
      f.getParameters.map(s => parseBound(s.getType)).toSeq,
      parseBound(f.getType),
      parseGenerics(f)
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