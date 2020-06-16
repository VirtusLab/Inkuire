package org.virtuslab.inkuire.engine.parser

import org.virtuslab.inkuire.engine.BaseInkuireTest
import org.virtuslab.inkuire.engine.model.{FunctionType, Signature, SignatureContext}
import org.virtuslab.inkuire.engine.model.Type._
import org.virtuslab.inkuire.engine.utils.syntax._

class KotilnSignatureParserFunctionTest extends BaseInkuireTest {

  it should "parse signature with a function receiver in parentheses" in {
    //given
    val str = "((Float) -> Double).(String)->Int"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          FunctionType(
            None,
            Seq(
              "Float".concreteType
            ),
            "Double".concreteType
          ).some,
          Seq(
            "String".concreteType
          ),
          "Int".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with a nullable function receiver in parentheses" in {
    //given
    val str = "(()->Int)?.() -> Unit"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          FunctionType(
            None,
            Seq.empty,
            "Int".concreteType
          ).?.some,
          Seq.empty,
          "Unit".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with a function result" in {
    //given
    val str = "Long . (String) -> (Float)->Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Long".concreteType.some,
          Seq(
            "String".concreteType
          ),
        FunctionType(
          None,
          Seq(
            "Float".concreteType
          ),
          "Double".concreteType
        ),
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with a nested function args" in {
    //given
    val str = "Long.(Int.(Float.() -> Long) -> Unit, Double) -> Float"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Long".concreteType.some,
          Seq(
            FunctionType(
              "Int".concreteType.some,
              Seq(
                FunctionType(
                  "Float".concreteType.some,
                  Seq.empty,
                  "Long".concreteType
                )
              ),
              "Unit".concreteType
            ),
            "Double".concreteType
          ),
          "Float".concreteType,
          SignatureContext.empty
        )
      )

    res should equal(expectedRes)
    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with piped functions as return types" in {
    //given
    val str = "Long.(String) -> (Float) -> Int.() -> Double.(Float) -> Unit"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Long".concreteType.some,
          Seq(
            "String".concreteType
          ),
          FunctionType(
            None,
            Seq(
              "Float".concreteType
            ),
            FunctionType(
              "Int".concreteType.some,
              Seq.empty,
              FunctionType(
                "Double".concreteType.some,
                Seq(
                  "Float".concreteType
                ),
                "Unit".concreteType
              )
            )
          ),
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }
}
