package org.virtuslab.inkuire.engine

import org.virtuslab.inkuire.engine.impl.service.EngineModelSerializers
import scala.io.Source

class DeserializationTest extends munit.FunSuite {

  def testDeserialization(version: String)(implicit loc: munit.Location): Unit = {
    test(s"Read ${version} stdlib") {
      val filePath: String = s"./engine/src/test/resources/stdlib-${version}.json"
      val source = Source.fromFile(filePath)
      val contents = source.mkString
      source.close()
      val res = EngineModelSerializers.deserialize(contents)
      assert(res.isRight)
    }
  }

  List(
    "3.1.3",
    "3.1.2",
    "3.1.1",
    "3.1.0",
    "3.0.2"
  ).foreach(testDeserialization)

}
