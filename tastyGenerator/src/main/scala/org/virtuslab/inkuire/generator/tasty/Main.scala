package org.virtuslab.inkuire.generator.tasty

import scala.quoted.*
import scala.tasty.inspector.*
import java.io.File
import java.io.FileWriter

import org.virtuslab.inkuire.engine.common.serialization.EngineModelSerializers

object Main extends App {
  val tastyFiles = List("/home/kkorban/cats")

  def getFiles(path: String): List[String] = {
    val file = new File(path)
    if (file.isFile) List(file).filter(_.getName.endsWith(".tasty")).map(_.getAbsolutePath)
    else file.listFiles.toList.map(_.getAbsolutePath).flatMap(getFiles)
  }

  TastyInspector.inspectTastyFiles(tastyFiles.flatMap(getFiles))(new InkuireInspector)

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
