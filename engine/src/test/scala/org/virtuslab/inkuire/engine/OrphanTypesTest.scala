package org.virtuslab.inkuire.engine

class OrphanTypesTest extends munit.FunSuite with BaseEndToEndEngineTest {

  override val filePath: String = "./engine/src/test/resources/orphantypes.json"

  testFunctionFound("Person => String", "name")

  testFunctionFound("Person => Int", "age")

  testFunctionFound("Person => Option[Address]", "address")

  testFunctionFound("Address => String", "street")

  testFunctionFound("Address => Int", "houseNumber")

  testFunctionFound("Address => String", "city")

}
