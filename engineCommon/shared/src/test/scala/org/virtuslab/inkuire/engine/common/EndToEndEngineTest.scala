package org.virtuslab.inkuire.engine.common

import java.io.File

class EndToEndEngineTest extends munit.FunSuite {
  val testService = new Fixture[InkuireTestService]("testService") {
    var testService: InkuireTestService = null
    def apply() = testService
    override def beforeAll(): Unit = {
      val file = new File("./engineCommon/shared/src/test/resources/inkuire-db.json")
      testService = new InkuireTestService(file.toURI.toURL.toString())
    }
    override def afterAll(): Unit = {}
  }
  override def munitFixtures = List(testService)

  test("map : List[A] => (A => B) => List[B]") {
    assert(testService().query("List[A] => (A => B) => List[B]").exists(_.name == "map"))
  }

  test("flatMap : List[A] => (A => List[B]) => List[B]") {
    assert(testService().query("List[A] => (A => List[B]) => List[B]").exists(_.name == "flatMap"))
  }
}