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

  parseCorrectSignature(
    "=> A",
    sgn(
      Seq.empty,
      tpe(
        "A",
        isVariable = true
      )
    )
  )

  parseCorrectSignature(
    "A => (A => A)",
    sgn(
      Seq(
        tpe(
          "A",
          isVariable = true
        ),
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

  parseCorrectSignature(
    "(A => A) => A",
    sgn(
      Seq(
        tpe(
          "Function1",
          params = Seq(
            tpe(
              "A",
              isVariable = true
            ),
            tpe(
              "A",
              isVariable = true
            )
          ),
          isVariable = false
        )
      ),
      tpe(
        "A",
        isVariable = true
      )
    )
  )

  parseCorrectSignature(
    "Int => String",
    sgn(
      Seq(
        tpe(
          "Int"
        )
      ),
      tpe(
        "String"
      )
    )
  )

  parseCorrectSignature(
    "Int => String => Long => Char",
    sgn(
      Seq(
        tpe(
          "Int"
        ),
        tpe(
          "String"
        ),
        tpe(
          "Long"
        )
      ),
      tpe(
        "Char"
      )
    )
  )

  parseCorrectSignature(
    "(Int, String) => Long",
    sgn(
      Seq(
        tpe(
          "Tuple2",
          params = Seq(
            tpe(
              "Int"
            ),
            tpe(
              "String"
            )
          )
        )
      ),
      tpe(
        "Long"
      )
    )
  )

  parseCorrectSignature(
    "Long => (Int, String)",
    sgn(
      Seq(
        tpe(
          "Long"
        )
      ),
      tpe(
        "Tuple2",
        params = Seq(
          tpe(
            "Int"
          ),
          tpe(
            "String"
          )
        )
      )
    )
  )

  parseCorrectSignature(
    "(Int => String => Char) => Long",
    sgn(
      Seq(
        tpe(
          "Function2",
          params = Seq(
            tpe(
              "Int"
            ),
            tpe(
              "String"
            ),
            tpe(
              "Char"
            )
          )
        )
      ),
      tpe(
        "Long"
      )
    )
  )

  parseCorrectSignature(
    "((Int, String) => Char) => Long",
    sgn(
      Seq(
        tpe(
          "Function2",
          params = Seq(
            tpe(
              "Int"
            ),
            tpe(
              "String"
            ),
            tpe(
              "Char"
            )
          )
        )
      ),
      tpe(
        "Long"
      )
    )
  )

  parseCorrectSignature(
    "Seq[Int] => Long",
    sgn(
      Seq(
        tpe(
          "Seq",
          params = Seq(
            tpe(
              "Int"
            )
          )
        )
      ),
      tpe(
        "Long"
      )
    )
  )

  parseCorrectSignature(
    "(Int & Double) => Long",
    sgn(
      Seq(
        AndType(
          tpe(
            "Int"
          ),
          tpe(
            "Double"
          )
        )
      ),
      tpe(
        "Long"
      )
    )
  )

  parseCorrectSignature(
    "Int => (Float | Double)",
    sgn(
      Seq(
        tpe(
        "Int"
        )
      ),
      AndType(
        tpe(
          "Float"
        ),
        tpe(
          "Double"
        )
      )
    )
  )

  parseCorrectSignature(
    "(Int & Long) => (Float | Double)",
    sgn(
      Seq(
        AndType(
          tpe(
            "Int"
          ),
          tpe(
            "Long"
          )
        )
      ),
      AndType(
        tpe(
          "Float"
        ),
        tpe(
          "Double"
        )
      )
    )
  )

}

trait ScalaParserTestUtils {
  import TypeName._

  def tpe(
    name:             String,
    params:           Seq[TypeLike] = Seq.empty,
    nullable:         Boolean = false,
    isVariable:       Boolean = false,
    isStarProjection: Boolean = false
  ): Type =
    Type(
      name = name,
      params = params.map(UnresolvedVariance),
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
