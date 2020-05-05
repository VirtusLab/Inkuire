package org.virtuslab.inkuire.engine.parser

import org.virtuslab.inkuire.engine.BaseInkuireTest
import org.virtuslab.inkuire.engine.model.{GenericType, Signature, SignatureContext}
import org.virtuslab.inkuire.engine.model.Type._
import org.virtuslab.inkuire.engine.utils.syntax._

class KotlinSignatureParserConstraintsTest extends BaseInkuireTest {

  it should "parse basic signature with a constraint" in {
    //given
    val str = "<A> A.() -> Int where A : Collection"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "A".typeVariable.some,
          Seq.empty,
          "Int".concreteType,
          SignatureContext(
            Set(
              "A"
            ),
            Map(
              "A" -> Seq("Collection".concreteType)
            )
          )
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with multiple constraints for one variable" in {
    //given
    val str = "<A> A.() -> Int where A : Collection, A : Iterable"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "A".typeVariable.some,
          Seq.empty,
          "Int".concreteType,
          SignatureContext(
            Set(
              "A"
            ),
            Map(
              "A" -> Seq(
                "Collection".concreteType,
                "Iterable".concreteType
              )
            )
          )
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with multiple constraints" in {
    //given
    val str = "<A, B> A.(Int) -> B where A : Collection, A : Iterable, B : CharSequence"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "A".typeVariable.some,
          Seq(
            "Int".concreteType
          ),
          "B".typeVariable,
          SignatureContext(
            Set(
              "B",
              "A"
            ),
            Map(
              "A" -> Seq(
                "Collection".concreteType,
                "Iterable".concreteType
              ),
              "B" -> Seq(
                "CharSequence".concreteType
              )
            )
          )
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with generic constraint" in {
    //given
    val str = "<A> A.(Int) -> String where A : Collection<Int>"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "A".typeVariable.some,
          Seq(
            "Int".concreteType
          ),
          "String".concreteType,
          SignatureContext(
            Set(
              "A"
            ),
            Map(
              "A" -> Seq(
                GenericType(
                  "Collection".concreteType,
                  Seq(
                    "Int".concreteType
                  )
                )
              )
            )
          )
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature with constraints overkill" in {
    //given
    val str = "<A, B, C> A.(B<Int>) -> List<C> where A : Any, A : B<Float>, A : List<C>, B : Any?, B : Collection"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "A".typeVariable.some,
          Seq(
            GenericType(
              "B".typeVariable,
              Seq(
                "Int".concreteType
              )
            )
          ),
          GenericType(
            "List".concreteType,
            Seq(
              "C".typeVariable
            )
          ),
          SignatureContext(
            Set(
              "A",
              "B",
              "C"
            ),
            Map(
              "A" -> Seq(
                "Any".concreteType,
                GenericType(
                  "B".typeVariable,
                  Seq(
                    "Float".concreteType
                  )
                ),
                GenericType(
                  "List".concreteType,
                  Seq(
                    "C".typeVariable
                  )
                )
              ),
              "B" -> Seq(
                "Any".concreteType.?,
                "Collection".concreteType
              )
            )
          )
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "return error when a constraint is defined for a non variable type" in {
    //given
    val str = "A.(Int) -> String where A : Collection"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then

    res should be(Symbol("left"))
  }
}
