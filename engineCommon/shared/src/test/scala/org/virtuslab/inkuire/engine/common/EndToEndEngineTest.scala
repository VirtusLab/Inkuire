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

  def testFunctionFound(signature: String, funName: String)(implicit loc: munit.Location): Unit = {
    test(s"$funName : $signature") {
      assert(testService().query(signature).exists(_.name == funName))
    }
  }

  testFunctionFound("List[A] => (A => B) => List[B]", "map")

  testFunctionFound("List[A] => (A => List[B]) => List[B]", "flatMap")

  testFunctionFound("List[Int] => (Int => List[Float]) => List[Float]", "flatMap")

  testFunctionFound("List[Int] => (Int => Float) => List[AnyVal]", "map")

  testFunctionFound("Seq[Int] => (Int => String) => Seq[String]", "map")

  testFunctionFound("A => (A => B) => B", "pipe")

  testFunctionFound("Char => (Char => B) => B", "pipe")

  testFunctionFound("Char => (Any => Double) => Double", "pipe")
}
