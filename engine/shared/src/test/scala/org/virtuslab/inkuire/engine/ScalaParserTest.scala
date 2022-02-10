package org.virtuslab.inkuire.engine

import org.virtuslab.inkuire.engine.impl.service.ScalaSignatureParserService
import org.virtuslab.inkuire.engine.impl.model._

class ScalaParserTest extends munit.FunSuite with ScalaParserTestUtils {

  val parser = new ScalaSignatureParserService

  def parseCorrectSignature(str: String, expected: Signature) = {
    test(str) {
      val res = parser.parse(str).map(_.signature)
      assertEquals(res, Right(expected))
    }
  }

  parseCorrectSignature(
    "A => A",
    sgn(
      Seq(
        tpe(
          "A",
          isVariable = true
        )
      ),
      tpe(
        "A",
        isVariable = true
      )
    )
  )

}

trait ScalaParserTestUtils {
  import TypeName._

  def tpe(
    name:             String,
    params:           Seq[Variance] = Seq.empty,
    nullable:         Boolean = false,
    isVariable:       Boolean = false,
    isStarProjection: Boolean = false
  ): Type =
    Type(
      name = name,
      params = params,
      nullable = nullable,
      itid = None,
      isVariable = isVariable,
      isStarProjection = isStarProjection,
      isUnresolved = false
    )

  def sgn(
    arguments: Seq[TypeLike],
    result:    TypeLike,
    context:   SignatureContext = SignatureContext.empty
  ): Signature =
    Signature(
      receiver = None,
      arguments = arguments,
      result = result,
      context = context
    )

}
