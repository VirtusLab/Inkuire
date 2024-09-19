package org.virtuslab.inkuire.engine

import java.io.File

trait BaseEndToEndEngineTest {
  self: munit.FunSuite =>

  val filePath: String

  val testService: Fixture[InkuireTestService] = new Fixture[InkuireTestService]("testService") {
    var testService: InkuireTestService = null
    def apply() = testService
    override def beforeAll(): Unit = {
      val url  = Thread.currentThread().getContextClassLoader().getResource(filePath)
      val file = new File(url.getPath())
      testService = new InkuireTestService(file.toURI.toURL.toString())
    }
    override def afterAll(): Unit = {}
  }
  override def munitFixtures: List[Fixture[InkuireTestService]] = List(testService)

  /**
   * Test whether a search using a `signature` includes `funName`
   */
  def testFunctionFound(signature: String, funName: String)(implicit loc: munit.Location): Unit = {
    test(s"$funName : $signature") {
      val sigs = testService().query(signature)
      assert(sigs.exists(_.name == funName))
    }
  }
}
