package org.virtuslab.inkuire.engine.model
import java.nio.file.{Files, Path}

import com.google.gson.Gson
import org.virtuslab.inkuire.model.{SDAnnotation, SDClass, SDEnum, SDEnumEntry, SDFunction, SDInterface, SDModule, SDObject}
import org.virtuslab.inkuire.engine.model.Type._

import scala.jdk.CollectionConverters._
import scala.io.Source
import util._

case class InkuireDb(
  functions: Seq[ExternalSignature],
  types: Map[Type, Set[Type]]
)

object InkuireDb {
  implicit def listAsScala[T](list: java.util.List[T]) : Iterable[T] = list.asScala

  private def generateFunctionSignature(f: SDFunction) = ExternalSignature(
    Signature(
      f.getReceiver.getName.concreteType,
      f.getParameters.map(s => s.getName.concreteType).toSeq,
      f.getReceiver.getName.concreteType,
      SignatureContext.empty
    ),
    f.getName,
    f.getDri
  )

  def read(path: Path): InkuireDb = {
    val source = Files.readString(path)
    parseSource(source)
  }

  def read(text: String): InkuireDb = parseSource(text)

  private def parseSource(source: String) : InkuireDb = {
    val module = new Gson().fromJson(source, classOf[SDModule])
    mapModule(module)
  }

  def mapModule(module: SDModule): InkuireDb = {

    val globalFunctions = module.getPackages.flatMap(p => p.getFunctions).map { f =>
      generateFunctionSignature(f)
    }

    val methods = module.getPackages.flatMap(p => p.getClasslikes).flatMap(c =>
      c match{
        case i: SDInterface => i.getFunctions
        case e: SDEnum => e.getFunctions ++ e.getEntries.flatMap(ee => ee.getFunctions)
        case o: SDObject => o.getFunctions
        case c: SDClass => c.getFunctions
        case a: SDAnnotation => a.getFunctions
        case _ => List.empty
    }
    ).map {
      f => generateFunctionSignature(f)
    }

    new InkuireDb((globalFunctions ++ methods).toSeq, Map.empty)
  }
}

case class ExternalSignature(
  signature: Signature,
  name: String,
  uri: String
)