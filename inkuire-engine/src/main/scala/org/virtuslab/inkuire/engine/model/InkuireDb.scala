package org.virtuslab.inkuire.engine.model
import java.nio.file.{Files, Path}

import com.google.gson.Gson
import org.virtuslab.inkuire.model._
import org.virtuslab.inkuire.engine.model.Type._
import org.virtuslab.inkuire.model.util.CustomGsonFactory
import com.softwaremill.quicklens._

import scala.jdk.CollectionConverters._
import scala.io.Source
import util._

import scala.annotation.tailrec
import scala.tools.nsc.io.File

case class InkuireDb(
  functions: Seq[ExternalSignature],
  types: Map[Type, Set[Type]]
)

object InkuireDb {
  implicit def listAsScala[T](list: java.util.List[T]) : Iterable[T] = list.asScala

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
    if(receiver == null) {
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

  def readFromPath(path: String): InkuireDb = {
    val source = Source.fromFile(path)
    val db = parseSource(source.getLines().mkString("\n"))
    source.close()
    db
  }

  def read(text: String): InkuireDb = parseSource(text)

  private def parseSource(source: String): InkuireDb = {
    val module = new CustomGsonFactory().getInstance().fromJson(source, classOf[SDModule])
    mapModule(module)
  }

  def mapModule(module: SDModule): InkuireDb = {
    val globalFunctions = module.getPackages.flatMap(_.getFunctions).map(generateFunctionSignature)

    val methods = module.getPackages.flatMap(_.getClasslikes).flatMap{ c =>
      c match {
        case i: SDInterface => i.getFunctions
        case e: SDEnum => e.getFunctions ++ e.getEntries.flatMap(ee => ee.getFunctions)
        case o: SDObject => o.getFunctions
        case c: SDClass => c.getFunctions
        case a: SDAnnotation => a.getFunctions
        case _ => List.empty
      }
    }.map (generateFunctionSignature)

    new InkuireDb((globalFunctions ++ methods).toSeq, Map.empty)
  }
}

case class ExternalSignature(
  signature: Signature,
  name: String,
  uri: String
)