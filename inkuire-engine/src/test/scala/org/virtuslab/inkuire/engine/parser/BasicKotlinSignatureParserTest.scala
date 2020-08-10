package org.virtuslab.inkuire.engine.parser

import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.BaseInkuireTest
import org.virtuslab.inkuire.engine.model.{Signature, SignatureContext}
import org.virtuslab.inkuire.engine.model.Type._

class BasicKotlinSignatureParserTest extends BaseInkuireTest {

  val parser = new KotlinSignatureParserService

  it should "parse simple signature" in {
    //given
    val str = "Int.(String) -> Double"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Int".concreteType.some,
          Seq(
            "String".concreteType
          ),
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse simple signature with spacings" in {
    //given
    val str = "Int . ( String ) -> Double"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Int".concreteType.some,
          Seq(
            "String".concreteType
          ),
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse simple signature v2137" in {
    //given
    val str = "Int . (String) -> Double"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Int".concreteType.some,
          Seq(
            "String".concreteType
          ),
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse simple signature without spacings" in {
    //given
    val str = "Int.(String)->Double"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Int".concreteType.some,
          Seq(
            "String".concreteType
          ),
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with multiple arguments" in {
    //given
    val str = "Int.(String, Long, Float) -> Double"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Int".concreteType.some,
          Seq(
            "String".concreteType,
            "Long".concreteType,
            "Float".concreteType
          ),
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature without arguments" in {
    //given
    val str = "Int.() -> Double"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Int".concreteType.some,
          Seq.empty,
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }
}
