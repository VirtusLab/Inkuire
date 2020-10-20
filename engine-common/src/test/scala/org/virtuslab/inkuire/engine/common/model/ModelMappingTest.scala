package org.virtuslab.inkuire.engine.common.model

import java.nio.file.Paths

import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.common.BaseInkuireTest

class ModelMappingTest extends BaseInkuireTest {
  it should "map global function" in {
    //given
    val source = Paths.get("src/test", "resources", "modelTestData", "functions", "1.json").toFile.toURI.toURL
    val any    = Paths.get("src/test", "resources", "modelTestData", "ancestors", "any.json").toFile.toURI.toURL

    //when
    val inkuire = InkuireDb.read(List(source), List(any))

    //then
    val expected = Seq(
      ExternalSignature(
        Signature(
          None,
          Seq.empty,
          ConcreteType("Unit", dri = DRI("kotlin".some, "Unit".some, None, "kotlin/Unit////").some),
          SignatureContext(Set.empty, Map.empty)
        ),
        "main",
        "example"
      )
    )
    inkuire.toOption.get.functions should matchTo(expected)
  }

  it should "map method" in {
    //given
    val source = Paths.get("src/test", "resources", "modelTestData", "functions", "2.json").toFile.toURI.toURL
    val any    = Paths.get("src/test", "resources", "modelTestData", "ancestors", "any.json").toFile.toURI.toURL
    val clock  = Paths.get("src/test", "resources", "modelTestData", "ancestors", "1.json").toFile.toURI.toURL
    //when
    val inkuire = InkuireDb.read(List(source), List(any, clock))

    //then
    val expected = Seq(
      ExternalSignature(
        Signature(
          Some(
            ConcreteType(
              "Clock",
              dri = DRI("example".some, "Clock".some, None, "example/Clock///PointingToDeclaration/").some
            )
          ),
          Seq.empty,
          ConcreteType("String", dri = DRI("kotlin".some, "String".some, None, "kotlin/String////").some),
          SignatureContext(Set.empty, Map.empty)
        ),
        "getTime",
        "example.Clock"
      )
    )
    inkuire.toOption.get.functions should matchTo(expected)
  }

  it should "load ancestors" in {
    //given
    val ancestors = Paths.get("src/test", "resources", "modelTestData", "ancestors", "1.json").toFile.toURI.toURL
    val any       = Paths.get("src/test", "resources", "modelTestData", "ancestors", "any.json").toFile.toURI.toURL

    //when
    val inkuire = InkuireDb.read(List.empty, List(ancestors, any))

    //then
    val expected: Map[DRI, (Type, Seq[Type])] = Map(
      DRI(
        "example".some,
        "ParticularClock".some,
        None,
        "example/ParticularClock///PointingToDeclaration/"
      ) ->
        (
          ConcreteType(
            "ParticularClock",
            false,
            DRI(
              "example".some,
              "ParticularClock".some,
              None,
              "example/ParticularClock///PointingToDeclaration/"
            ).some
          ),
          Seq(
            ConcreteType(
              "InterfaceToInheritFrom",
              false,
              DRI(
                "example".some,
                "InterfaceToInheritFrom".some,
                None,
                "example/InterfaceToInheritFrom///PointingToDeclaration/"
              ).some
            ),
            ConcreteType(
              "Clock",
              false,
              DRI(
                "example".some,
                "Clock".some,
                None,
                "example/Clock///PointingToDeclaration/"
              ).some
            )
          )
      ),
      DRI(
        "kotlin".some,
        "Any".some,
        None,
        "kotlin/Any///PointingToDeclaration/"
      ) -> (
        ConcreteType(
          "Any",
          false,
          DRI(
            "kotlin".some,
            "Any".some,
            None,
            "kotlin/Any///PointingToDeclaration/"
          ).some
        ),
        Seq.empty
      ),
      DRI(
        "example".some,
        "Clock".some,
        None,
        "example/Clock///PointingToDeclaration/"
      ) ->
        (
          ConcreteType(
            "Clock",
            false,
            DRI(
              "example".some,
              "Clock".some,
              None,
              "example/Clock///PointingToDeclaration/"
            ).some
          ),
          Seq(
            ConcreteType(
              "Any",
              false,
              DRI(
                "kotlin".some,
                "Any".some,
                None,
                "kotlin/Any///PointingToDeclaration/"
              ).some
            )
          )
      ),
      DRI(
        "kotlin".some,
        "Any".some,
        None,
        "kotlin/Any///PointingToDeclaration/"
      ) -> (
        ConcreteType(
          "Any",
          false,
          DRI(
            "kotlin".some,
            "Any".some,
            None,
            "kotlin/Any///PointingToDeclaration/"
          ).some
        ),
        Seq.empty
      )
    )

    inkuire.toOption.get.types should matchTo(expected)
  }
}
