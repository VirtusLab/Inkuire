package org.virtuslab.inkuire.engine.model

import com.google.gson.Gson
import org.virtuslab.inkuire.model.{SDModule}
import org.virtuslab.inkuire.engine.model.Type._
import scala.collection.JavaConverters._
import scala.io.Source

case class InkuireDb(
  functions: Seq[ExternalSignature],
  types: Map[Type, Set[Type]]
)

object InkuireDb {
  def read(path: String): InkuireDb = {

    val source = Source.fromFile(path).bufferedReader()
    val module: SDModule = new Gson().fromJson(source, classOf[SDModule])

    val functions = module.getPackages.asScala.flatMap(p => p.getFunctions.asScala).map { f =>
      ExternalSignature(
        Signature(
          f.getReceiver.getName.concreteType,
          f.getParameters.asScala.map(s => s.getName.concreteType).toSeq,
          f.getReceiver.getName.concreteType,
          SignatureContext.empty
        ),
        f.getName,
        f.getDri
      )
    }

    new InkuireDb(functions.toSeq, Map.empty)
  }
}

case class ExternalSignature(
  signature: Signature,
  name: String,
  uri: String
)