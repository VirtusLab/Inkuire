package org.virtuslab.inkuire.engine.parser

import org.virtuslab.inkuire.engine.BaseInkuireTest
import org.virtuslab.inkuire.engine.model.{GenericType, Signature, SignatureContext}
import org.virtuslab.inkuire.engine.model.Type._
import org.virtuslab.inkuire.engine.utils.syntax._

class KotlinSignatureParserNoReceiverTest extends BaseInkuireTest {

  val parser = new KotlinSignatureParserService

  it should "parse signature without receiver" in {
    //given
    val str = "() -> Double"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          None,
          Seq.empty,
          "Double".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }

  it should "parse main" in {
    //given
    val str = "(Array<String>) -> Unit"

    //when
    val res = parser.parse(str)

    //then
    val expectedRes =
      Right(
        Signature(
          None,
          Seq(
            GenericType(
              "Array".concreteType,
              Seq(
                "String".concreteType
              )
            )
          ),
          "Unit".concreteType,
          SignatureContext.empty
        )
      )

    res should matchTo[Either[String, Signature]](expectedRes)
  }
}
