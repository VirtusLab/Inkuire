package org.virtuslab.inkuire.engine.parser

import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.BaseInkuireTest
import org.virtuslab.inkuire.engine.model.{GenericType, Signature, SignatureContext}
import org.virtuslab.inkuire.engine.model.Type._

class KotlinSignatureParserNullabilityTest extends BaseInkuireTest {

  val parser = new KotlinSignatureParserService

  it should "parse signature with nullable receiver" in {
    //given
    val str = "Int?.() -> Double"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Int".concreteType.?.some,
          Seq.empty,
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with nullable result" in {
    //given
    val str = "Int?.() -> Double"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Int".concreteType.?.some,
          Seq.empty,
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with nullable argument" in {
    //given
    val str = "Int.(String?, Double) -> Double"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Int".concreteType.some,
          Seq(
            "String".concreteType.?,
            "Double".concreteType
          ),
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with nullable generic result" in {
    //given
    val str = "Short.(Double) -> Array<Float>?"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Short".concreteType.some,
          Seq(
            "Double".concreteType
          ),
          GenericType(
            "Array".concreteType.?,
            Seq(
              "Float".concreteType
            )
          ),
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with nullable generic receiver" in {
    //given
    val str = "Array<Short>?.(Double) -> Double"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          GenericType(
            "Array".concreteType.?,
            Seq(
              "Short".concreteType
            )
          ).some,
          Seq(
            "Double".concreteType
          ),
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with generic argument with nullable parameter" in {
    //given
    val str = "Array<Short?>.(Double) -> Double"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          GenericType(
            "Array".concreteType,
            Seq(
              "Short".concreteType.?
            )
          ).some,
          Seq(
            "Double".concreteType
          ),
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with nullable variable" in {
    //given
    val str = "<A> Array<Short>.(Double) -> A?"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          GenericType(
            "Array".concreteType,
            Seq(
              "Short".concreteType
            )
          ).some,
          Seq(
            "Double".concreteType
          ),
          "A".typeVariable.?,
          SignatureContext(
            Set(
              "A"
            ),
            Map.empty
          )
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "null result in function type if there are no parentheses" in {
    //given
    val str = "<A> Byte.( ()->Int? ) -> A?"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Byte".concreteType.some,
          Seq(
            GenericType(
              "Function0".concreteType,
              Seq(
                "Int".concreteType.?
              )
            )
          ),
          "A".typeVariable.?,
          SignatureContext(
            Set(
              "A"
            ),
            Map.empty
          )
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "null function type if it is in parentheses" in {
    //given
    val str = "<A> Byte.( (()->Int)? ) -> Array<A>?"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Byte".concreteType.some,
          Seq(
            GenericType(
              "Function0".concreteType,
              Seq(
                "Int".concreteType
              )
            ).?
          ),
          GenericType(
            "Array".concreteType.?,
            Seq(
              "A".typeVariable
            )
          ),
          SignatureContext(
            Set(
              "A"
            ),
            Map.empty
          )
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }
}
