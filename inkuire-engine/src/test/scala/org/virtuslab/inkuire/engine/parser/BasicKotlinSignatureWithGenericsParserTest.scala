package org.virtuslab.inkuire.engine.parser

import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.BaseInkuireTest
import org.virtuslab.inkuire.engine.model.{GenericType, Signature, SignatureContext}
import org.virtuslab.inkuire.engine.model.Type._
import org.virtuslab.inkuire.engine.utils.syntax._
import org.virtuslab.inkuire.engine.model.StarProjection

class BasicKotlinSignatureWithGenericsParserTest extends BaseInkuireTest {

  val parser = new KotlinSignatureParserService

  it should "parse signature with generic return type" in {
    //given
    val str = "String.(Int) -> List<String>"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "String".concreteType.some,
          Seq("Int".concreteType),
          GenericType(
            "List".concreteType,
            Seq(
              "String".concreteType
            )
          ),
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with generic receiver" in {
    //given
    val str = "Array<Double>.(Int) -> String"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          GenericType(
            "Array".concreteType,
            Seq(
              "Double".concreteType
            )
          ).some,
          Seq(
            "Int".concreteType
          ),
          "String".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with generic receiver v2137" in {
    //given
    val str = "Array < Double > . (Int) -> String"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          GenericType(
            "Array".concreteType,
            Seq(
              "Double".concreteType
            )
          ).some,
          Seq(
            "Int".concreteType
          ),
          "String".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with generic argument" in {
    //given
    val str = "Float.(List<Double>) -> String"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Float".concreteType.some,
          Seq(
            GenericType(
              "List".concreteType,
              Seq(
                "Double".concreteType
              )
            )
          ),
          "String".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with generic arguments" in {
    //given
    val str = "Float.(List<Double>, Array<Int>) -> String"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Float".concreteType.some,
          Seq(
            GenericType(
              "List".concreteType,
              Seq(
                "Double".concreteType
              )
            ),
            GenericType(
              "Array".concreteType,
              Seq(
                "Int".concreteType
              )
            )
          ),
          "String".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with generic argument with multiple type params" in {
    //given
    val str = "Float.(Map<Double, Float>) -> String"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Float".concreteType.some,
          Seq(
            GenericType(
              "Map".concreteType,
              Seq(
                "Double".concreteType,
                "Float".concreteType
              )
            )
          ),
          "String".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with generic with star projection" in {
    //given
    val str = "Float.(Map<*, Float>) -> String"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Float".concreteType.some,
          Seq(
            GenericType(
              "Map".concreteType,
              Seq(
                StarProjection,
                "Float".concreteType
              )
            )
          ),
          "String".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }
}
