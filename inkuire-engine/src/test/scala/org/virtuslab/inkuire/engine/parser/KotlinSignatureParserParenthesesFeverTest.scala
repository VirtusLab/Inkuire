package org.virtuslab.inkuire.engine.parser

import org.virtuslab.inkuire.engine.BaseInkuireTest
import org.virtuslab.inkuire.engine.model.{FunctionType, GenericType, Signature, SignatureContext}
import org.virtuslab.inkuire.engine.model.Type._
import org.virtuslab.inkuire.engine.utils.syntax._

class KotlinSignatureParserParenthesesFeverTest extends BaseInkuireTest {

  it should "parse basic signature with many unnecessary parentheses" in {
    //given
    val str = "(Int).((String), Double) -> (Unit)"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Int".concreteType.some,
          Seq(
            "String".concreteType,
            "Double".concreteType
          ),
          "Unit".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse generic signature with many unnecessary parentheses" in {
    //given
    val str = "<A> (((A?))?).( (((String) ) ), Double?) -> (Array<((A))>)"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "A".typeVariable.?.some,
          Seq(
            "String".concreteType,
            "Double".concreteType.?
          ),
          GenericType(
            "Array".concreteType,
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

  it should "parse signature with function type and many unnecessary parentheses" in {
    //given
    val str = "( (((String) ).(Byte) -> (((String)))?  ), Double? ) -> (Array<((Short))>?)"

    //when
    val res = KotlinSignatureParser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          None,
          Seq(
            FunctionType(
              "String".concreteType.some,
              Seq(
                "Byte".concreteType
              ),
              "String".concreteType.?
            ),
            "Double".concreteType.?
          ),
          GenericType(
            "Array".concreteType.?,
            Seq(
              "Short".concreteType
            )
          ),
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse signature in parentheses" in {
    //given
    val str = "((<B>B.(List<Byte>)->Double))"

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
}
