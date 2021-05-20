package org.virtuslab.inkuire.generator.tasty

import scala.quoted.*
import scala.tasty.inspector.*
import java.io.File
import java.io.FileWriter

import org.virtuslab.inkuire.engine.common.serialization.EngineModelSerializers

object Main extends App {
  val tastyFiles = List("tastyGenerator/target/scala-3.0.0/classes/org/virtuslab/inkuire/generator/tasty/Main.tasty")

  TastyInspector.inspectTastyFiles(tastyFiles)(new InkuireInspector)

  dumpInkuireDB()
}

def dumpInkuireDB() = {
  println(s"Types: ${InkuireDB.db.types.size}")
  println(s"Functions: ${InkuireDB.db.functions.size}")
  val file = new File("./inkuire-db.json")
  file.createNewFile()
  val myWriter = new FileWriter("./inkuire-db.json", false)
  myWriter.write(s"${EngineModelSerializers.serialize(InkuireDB.db)}")
  myWriter.close()
}
