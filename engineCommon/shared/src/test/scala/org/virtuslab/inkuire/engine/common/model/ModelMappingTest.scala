package org.virtuslab.inkuire.engine.common.model

import java.net.URL
import java.nio.file.Paths

import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.common.BaseInkuireTest

import scala.io.Source

class ModelMappingTest extends BaseInkuireTest {

  ignore should "map global function" in {
    //given
    val source =
      urlToContent(Paths.get("src/test", "resources", "modelTestData", "functions", "1.json").toFile.toURI.toURL)
    val any =
      urlToContent(Paths.get("src/test", "resources", "modelTestData", "ancestors", "any.json").toFile.toURI.toURL)

    //when
    val inkuire = InkuireDb.read(List(source), List(any))

    //then
    val expected = Seq(
      ExternalSignature(
        Signature(
          None,
          Seq.empty,
          ConcreteType("Unit", itid = ITID("kotlin/Unit////", isParsed = false).some),
          SignatureContext(Set.empty, Map.empty)
        ),
        "main",
        "example",
        ""
      )
    )
    inkuire.toOption.get.functions should matchTo(expected)
  }

  ignore should "map method" in {
    //given
    val source =
      urlToContent(Paths.get("src/test", "resources", "modelTestData", "functions", "2.json").toFile.toURI.toURL)
    val any =
      urlToContent(Paths.get("src/test", "resources", "modelTestData", "ancestors", "any.json").toFile.toURI.toURL)
    val clock =
      urlToContent(Paths.get("src/test", "resources", "modelTestData", "ancestors", "1.json").toFile.toURI.toURL)
    //when
    val inkuire = InkuireDb.read(List(source), List(any, clock))

    //then
    val expected = Seq(
      ExternalSignature(
        Signature(
          Some(
            ConcreteType(
              "Clock",
              itid = ITID("example/Clock///PointingToDeclaration/", isParsed = false).some
            )
          ),
          Seq.empty,
          ConcreteType("String", itid = ITID("kotlin/String////", isParsed = false).some),
          SignatureContext(Set.empty, Map.empty)
        ),
        "getTime",
        "example.Clock",
        ""
      )
    )
    inkuire.toOption.get.functions should matchTo(expected)
  }

  ignore should "load ancestors" in {
    def urlToContent(url: URL) = Source.fromInputStream(url.openStream()).getLines().mkString
    //given
    val ancestors =
      urlToContent(Paths.get("src/test", "resources", "modelTestData", "ancestors", "1.json").toFile.toURI.toURL)
    val any =
      urlToContent(Paths.get("src/test", "resources", "modelTestData", "ancestors", "any.json").toFile.toURI.toURL)

    //when
    val inkuire = InkuireDb.read(List.empty, List(ancestors, any))

    //then
    val expected: Map[ITID, (Type, Seq[Type])] = Map(
      ITID("example/ParticularClock///PointingToDeclaration/", isParsed = false) ->
        (
          ConcreteType(
            "ParticularClock",
            false,
            ITID("example/ParticularClock///PointingToDeclaration/", isParsed = false).some
          ),
          Seq(
            ConcreteType(
              "InterfaceToInheritFrom",
              false,
              ITID("example/InterfaceToInheritFrom///PointingToDeclaration/", isParsed = false).some
            ),
            ConcreteType(
              "Clock",
              false,
              ITID("example/Clock///PointingToDeclaration/", isParsed = false).some
            )
          )
      ),
      ITID("kotlin/Any///PointingToDeclaration/", isParsed = false) -> (
        ConcreteType(
          "Any",
          false,
          ITID("kotlin/Any///PointingToDeclaration/", isParsed = false).some
        ),
        Seq.empty
      ),
      ITID("example/Clock///PointingToDeclaration/", isParsed = false) ->
        (
          ConcreteType(
            "Clock",
            false,
            ITID("example/Clock///PointingToDeclaration/", isParsed = false).some
          ),
          Seq(
            ConcreteType(
              "Any",
              false,
              ITID("kotlin/Any///PointingToDeclaration/", isParsed = false).some
            )
          )
      ),
      ITID("kotlin/Any///PointingToDeclaration/", isParsed = false) -> (
        ConcreteType(
          "Any",
          false,
          ITID("kotlin/Any///PointingToDeclaration/", isParsed = false).some
        ),
        Seq.empty
      )
    )

    inkuire.toOption.get.types should matchTo(expected)
  }

  def urlToContent(url: URL) = Source.fromInputStream(url.openStream()).getLines().mkString
}
