package org.virtuslab.inkuire.engine.model

import org.virtuslab.inkuire.model._
import org.virtuslab.inkuire.engine.model.Type._
import org.virtuslab.inkuire.model.util.CustomGson
import scala.jdk.CollectionConverters._
import scala.io.Source
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

  def readFromPath(functionsPaths: List[String], inheritancePaths: List[String]): InkuireDb = {
    val source = Source.fromFile(functionsPaths.head) // TODO: Fix this plug
    val db = parseSource(source.getLines().mkString("\n"))
    source.close()
    db
  }

  def read(text: String): InkuireDb = parseSource(text)

  private def parseSource(source: String): InkuireDb = {
    val functions = CustomGson.INSTANCE.getWithSDocumentableAdapters.fromJson(source, classOf[Array[SDFunction]]) // Workaround for Gson cannot deserialize directly to Scala List
    unwrapToInkuireDb(functions.toList)
  }

  def unwrapToInkuireDb(functions: List[SDFunction]): InkuireDb = {
    new InkuireDb(functions.map(generateFunctionSignature), Map.empty)
  }
}

case class ExternalSignature(
  signature: Signature,
  name: String,
  uri: String
)