package org.virtuslab.inkuire.engine.common.parser

import org.virtuslab.inkuire.engine.common.BaseInkuireTest
import org.virtuslab.inkuire.engine.common.model.{GenericType, Signature, SignatureContext, UnresolvedVariance}
import org.virtuslab.inkuire.engine.common.model.Signature
import org.virtuslab.inkuire.engine.common.model.Type._
import org.virtuslab.inkuire.engine.common.utils.syntax._

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
                UnresolvedVariance(
                  "String".concreteType
                )
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
