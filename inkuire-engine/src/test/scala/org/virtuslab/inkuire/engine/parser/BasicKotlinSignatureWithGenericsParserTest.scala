package org.virtuslab.inkuire.engine.parser

import org.virtuslab.inkuire.engine.BaseInkuireTest
import org.virtuslab.inkuire.engine.model.{GenericType, Signature, SignatureContext}
import org.virtuslab.inkuire.engine.model.SignatureContext
import org.virtuslab.inkuire.engine.model.Type._

class BasicKotlinSignatureWithGenericsParserTest extends BaseInkuireTest {

  it should "parse signature with generic return type" in {
    //given
    val str = "String.(Int) -> List<String>"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "String".concreteType,
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

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with generic receiver" in {
    //given
    val str = "Array<Double>.(Int) -> String"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          GenericType(
            "Array".concreteType,
            Seq(
              "Double".concreteType
            )
          ),
          Seq(
            "Int".concreteType
          ),
          "String".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with generic receiver v2137" in {
    //given
    val str = "Array < Double > . (Int) -> String"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          GenericType(
            "Array".concreteType,
            Seq(
              "Double".concreteType
            )
          ),
          Seq(
            "Int".concreteType
          ),
          "String".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with generic argument" in {
    //given
    val str = "Float.(List<Double>) -> String"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Float".concreteType,
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

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with generic arguments" in {
    //given
    val str = "Float.(List<Double>, Array<Int>) -> String"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Float".concreteType,
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

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with generic argument with multiple type params" in {
    //given
    val str = "Float.(Map<Double, Float>) -> String"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Float".concreteType,
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

    res should matchTo[Either[String, Signature]] (expectedRes)
  }
}
