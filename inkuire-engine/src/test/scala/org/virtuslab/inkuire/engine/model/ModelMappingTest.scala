package org.virtuslab.inkuire.engine.model

import java.nio.file.Paths
import org.virtuslab.inkuire.engine.BaseInkuireTest

class ModelMappingTest extends BaseInkuireTest {
  it should "map global function" in {
    //given
    val source = Paths.get("src/test", "resources", "modelTestData", "functions", "1.json").toFile

    //when
    val inkuire = InkuireDb.read(List(source), List.empty)

    //then
    val expected = Seq(
      ExternalSignature(Signature(
        None, Seq.empty, ConcreteType("Unit"), SignatureContext(Set.empty, Map.empty)), "main", "example//main/#//")
    )
    inkuire.functions should matchTo(expected)
  }

  it should "map method" in {
    //given
    val source = Paths.get("src/test", "resources", "modelTestData", "functions", "2.json").toFile

    //when
    val inkuire = InkuireDb.read(List(source), List.empty)

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

  it should "load ancestors" in {
    //given
    val ancestors = Paths.get("src/test", "resources", "modelTestData", "ancestors", "1.json").toFile

    //when
    val inkuire = InkuireDb.read(List.empty, List(ancestors))

    //then
    val expected: Map[Type, Set[Type]] = Map(
      ConcreteType("example/ParticularClock///PointingToDeclaration/") -> Set(
        ConcreteType("example/Clock///PointingToDeclaration/"),
        ConcreteType("example/InterfaceToInheritFrom///PointingToDeclaration/")
      )
    )
    inkuire.types should matchTo(expected)
  }
}
