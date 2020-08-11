package org.virtuslab.inkuire.engine.parser

import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.BaseInkuireTest
import org.virtuslab.inkuire.engine.model.{GenericType, Signature, SignatureContext}
import org.virtuslab.inkuire.engine.model.Type._

class KotlinSignatureParserConstraintsTest extends BaseInkuireTest {

  val parser = new KotlinSignatureParserService

  it should "parse basic signature with a constraint" in {
    //given
    val str = "<A> A.() -> Int where A : Collection"

    //when
    val res = parser.parse(str)

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
    val res = parser.parse(str)

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
    val res = parser.parse(str)

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
    val res = parser.parse(str)

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
    val str =
      "<A, B, C> A.(B) -> List<C> where A : Any, A : Collection<Float>, A : List<C>, B : Any?, B : Collection<Int>"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "A".typeVariable.some,
          Seq(
            "B".typeVariable,
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
                  "Collection".concreteType,
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

  it should "return error when a constraint is defined for a non variable type" in {
    //given
    val str = "A.(Int) -> String where A : Collection"

    //when
    val res = parser.parse(str)

    //then
    res should be(Symbol("left"))
  }

  it should "return error when type arguments are used for type parameters in constraints" in {
    //given
    val str = "<A, B> A.(Int) -> String where A : B<Int>"

    //when
    val res = parser.parse(str)

    //then
    res should be(Symbol("left"))
  }

  it should "parse function as upper bound" in {
    //given
    val str = "<A> A.(Int) -> String where A : (Int) -> String"

    //when
    val res = parser.parse(str)

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
                  "Function1".concreteType,
                  Seq(
                    "Int".concreteType,
                    "String".concreteType
                  )
                )
              )
            )
          )
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  ignore should "return error when function with receiver is used as upper bound" in {
    //given
    val str = "<A> A.(Int) -> String where A : Int.() -> String"

    //when
    val res = parser.parse(str)

    //then
    res should be(Symbol("left"))
  }

  it should "parse type variable with single constraint syntactic sugar" in {
    //given
    val str = "<A: Array<Int>> A.(Int) -> String"

    //when
    val res = parser.parse(str)

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
                  "Array".concreteType,
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

  it should "parse type variable with single constraint syntactic sugar and additional constraint" in {
    //given
    val str = "<A: Array<Int>> A.(Int) -> String where A : Collection<Int>"

    //when
    val res = parser.parse(str)

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
                ),
                GenericType(
                  "Array".concreteType,
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
}
