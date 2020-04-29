package org.virtuslab.inkuire.engine.parser

import org.virtuslab.inkuire.engine.model
import org.virtuslab.inkuire.engine.model.{GenericType, Signature, SignatureContext}
import org.virtuslab.inkuire.engine.model.SignatureContext
import org.virtuslab.inkuire.engine.model.Type._

class KotlinSignatureParserNullabilityTest extends BaseInkuireTest {

  it should "parse signature with nullable receiver" in {
    //given
    val str = "Int?.() -> Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Int".concreteType.?,
          Seq.empty,
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with nullable result" in {
    //given
    val str = "Int?.() -> Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        model.Signature(
          "Int".concreteType.?,
          Seq.empty,
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with nullable argument" in {
    //given
    val str = "Int.(String?, Double) -> Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        model.Signature(
          "Int".concreteType,
          Seq(
            "String".concreteType.?,
            "Double".concreteType
          ),
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with nullable generic result" in {
    //given
    val str = "Short.(Double) -> Array<Float>?"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        model.Signature(
          "Short".concreteType,
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

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with nullable generic receiver" in {
    //given
    val str = "Array<Short>?.(Double) -> Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        model.Signature(
          GenericType(
            "Array".concreteType.?,
            Seq(
              "Short".concreteType
            )
          ),
          Seq(
            "Double".concreteType
          ),
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with generic argument with nullable parameter" in {
    //given
    val str = "Array<Short?>.(Double) -> Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        model.Signature(
          GenericType(
            "Array".concreteType,
            Seq(
              "Short".concreteType.?
            )
          ),
          Seq(
            "Double".concreteType
          ),
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with nullable variable" in {
    //given
    val str = "<A> Array<Short>.(Double) -> A?"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        model.Signature(
          GenericType(
            "Array".concreteType,
            Seq(
              "Short".concreteType
            )
          ),
          Seq(
            "Double".concreteType
          ),
          "A".typeVariable.?,
          SignatureContext(
            Set(
              "A".typeVariable
            ),
            Map.empty
          )
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with nullable variable as generic" in {
    //given
    val str = "<A> Array<Short>.(Double) -> A<Long>?"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        model.Signature(
          GenericType(
            "Array".concreteType,
            Seq(
              "Short".concreteType
            )
          ),
          Seq(
            "Double".concreteType
          ),
          GenericType(
            "A".typeVariable.?,
            Seq(
              "Long".concreteType
            )
          ),
          SignatureContext(
            Set(
              "A".typeVariable
            ),
            Map.empty
          )
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }
}
