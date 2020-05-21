package org.virtuslab.inkuire.engine.model

import java.nio.file.{Files, Paths}

import org.virtuslab.inkuire.engine.BaseInkuireTest

class ModelMappingTest extends BaseInkuireTest {
  it should "map global function" in {
    //given
    val source = Paths.get("src/test", "resources", "modelTestData","1.json")

    //when
    val inkuire = InkuireDb.read(source)

    //then
    val expected = Seq(
      ExternalSignature(Signature(
        None, Seq.empty, ConcreteType("Unit"), SignatureContext(Set.empty, Map.empty)), "main", "example//main/#//")
    )
    inkuire.functions should matchTo(expected)
  }

  it should "map method" in {
    //given
    val source = Paths.get("src/test", "resources", "modelTestData","2.json")

    //when
    val inkuire = InkuireDb.read(source)

    //then
    val expected = Seq(
      ExternalSignature(
        Signature(
          Some(ConcreteType("Clock")), Seq.empty, ConcreteType("String"), SignatureContext(Set.empty, Map.empty)
        ),
        "getTime",
        "example/Clock/getTime/#//"
      )
    )
    inkuire.functions should matchTo(expected)
  }
}