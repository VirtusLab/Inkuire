package org.virtuslab.inkuire.engine.common.parser

import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.common.BaseInkuireTest
import org.virtuslab.inkuire.engine.common.model.{Signature, SignatureContext, StarProjection}
import org.virtuslab.inkuire.engine.common.model.Signature
import org.virtuslab.inkuire.engine.common.model.Type._

class KotlinSignatureParserStarProjectionTest extends BaseInkuireTest {

  val parser = new KotlinSignatureParserService

  it should "parse star projection as result type" in {
    //given
    val str = "Int.(String) -> *"

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
          StarProjection,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse star projection as receiver" in {
    //given
    val str = "*.(String) -> Double"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          StarProjection.some,
          Seq(
            "String".concreteType
          ),
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse star projection as argument" in {
    //given
    val str = "Int.(*, String) -> Double"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          "Int".concreteType.some,
          Seq(
            StarProjection,
            "String".concreteType
          ),
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

}
