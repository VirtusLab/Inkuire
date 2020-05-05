package org.virtuslab.inkuire.engine.parser

import org.virtuslab.inkuire.engine.BaseInkuireTest
import org.virtuslab.inkuire.engine.model.{GenericType, Signature, SignatureContext}
import org.virtuslab.inkuire.engine.model.Type._
import org.virtuslab.inkuire.engine.utils.syntax._

class GenericKotlinSignatureParserTest extends BaseInkuireTest {

  it should "parse signature with type variable as result" in {
    //given
    val str = "<A> Float.(Int) -> A"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Float".concreteType.some,
          Seq(
            "Int".concreteType
          ),
          "A".typeVariable,
          SignatureContext(
            Set(
              "A"
            ),
            Map.empty
          )
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with type variable as receiver" in {
    //given
    val str = "<B>B.(List<Byte>)->Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "B".typeVariable.some,
          Seq(
            GenericType(
              "List".concreteType,
              Seq(
                "Byte".concreteType
              )
            )
          ),
          "Double".concreteType,
          SignatureContext(
            Set(
              "B"
            ),
            Map.empty
          )
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with type variable as base type in generic" in {
    //given
    val str = "<A> Int.(A<Byte>)->Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Int".concreteType.some,
          Seq(
            GenericType(
              "A".typeVariable,
              Seq(
                "Byte".concreteType
              )
            )
          ),
          "Double".concreteType,
          SignatureContext(
            Set(
              "A"
            ),
            Map.empty
          )
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with type variable as parameter type in generic" in {
    //given
    val str = "<XD> Int.(List<XD>)-> Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Int".concreteType.some,
          Seq(
            GenericType(
              "List".concreteType,
              Seq(
                "XD".typeVariable
              )
            )
          ),
          "Double".concreteType,
          SignatureContext(
            Set(
              "XD"
            ),
            Map.empty
          )
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with type variable as deeply nested parameter type in generic" in {
    //given
    val str = "<x> Float.(List<Array<Set<x>>>)-> Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Float".concreteType.some,
          Seq(
            GenericType(
              "List".concreteType,
              Seq(
                GenericType(
                  "Array".concreteType,
                  Seq(
                    GenericType(
                      "Set".concreteType,
                      Seq(
                        "x".typeVariable
                      )
                    )
                  )
                )
              )
            )
          ),
          "Double".concreteType,
          SignatureContext(
            Set(
              "x"
            ),
            Map.empty
          )
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with type variable in the middle of deeply nested parameter types" in {
    //given
    val str = "<x> Float.(List<x<Set<Float>>>)-> Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Float".concreteType.some,
          Seq(
            GenericType(
              "List".concreteType,
              Seq(
                GenericType(
                  "x".typeVariable,
                  Seq(
                    GenericType(
                      "Set".concreteType,
                      Seq(
                        "Float".concreteType
                      )
                    )
                  )
                )
              )
            )
          ),
          "Double".concreteType,
          SignatureContext(
            Set(
              "x"
            ),
            Map.empty
          )
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }

  it should "parse signature with multiple type variables" in {
    //given
    val str = "<A, B> A<Int>.(B, Float)-> Double"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          GenericType(
            "A".typeVariable,
            Seq(
              "Int".concreteType
            )
          ).some,
          Seq(
            "B".typeVariable,
            "Float".concreteType
          ),
          "Double".concreteType,
          SignatureContext(
            Set(
              "A",
              "B"
            ),
            Map.empty
          )
        )
      )

    res should matchTo[Either[String, Signature]] (expectedRes)
  }
}
