package org.virtuslab.inkuire.engine.common.service

import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.common.model.{
  ExternalSignature,
  GenericType,
  InkuireDb,
  Signature,
  SignatureContext,
  UnresolvedVariance
}
import org.virtuslab.inkuire.engine.common.model._
import org.virtuslab.inkuire.engine.common.model.Type._
import ExactMatchServiceTest.Fixture
import org.virtuslab.inkuire.engine.common.BaseInkuireTest

class ExactMatchServiceTest extends BaseInkuireTest {

  it should "match simple function" in new Fixture {
    //given
    val exactMatchService = new ExactMatchService(
      InkuireDb(
        Seq(
          equalsExternalSignature
        ),
        Map.empty
      )
    )
    //when
    val res: Seq[ExternalSignature] = exactMatchService |??| ResolveResult(Seq(equalsSignature))

    //then
    res should matchTo[Seq[ExternalSignature]](Seq(equalsExternalSignature))
  }

  it should "match not match wrong functions" in new Fixture {
    //given
    val exactMatchService = new ExactMatchService(
      InkuireDb(
        Seq(
          equalsExternalSignature
        ),
        Map.empty
      )
    )
    //when
    val res: Seq[ExternalSignature] = exactMatchService |??| ResolveResult(Seq(toStringSignature))

    //then
    res should matchTo[Seq[ExternalSignature]](Seq.empty)
  }

  it should "match signature with generics" in new Fixture {
    //given
    val exactMatchService = new ExactMatchService(
      InkuireDb(
        Seq(
          rangeExternalSignature
        ),
        Map.empty
      )
    )
    //when
    val res: Seq[ExternalSignature] = exactMatchService |??| ResolveResult(Seq(rangeSignature))

    //then
    res should matchTo[Seq[ExternalSignature]](Seq(rangeExternalSignature))
  }

  it should "match signature without receiver" in new Fixture {
    //given
    val exactMatchService = new ExactMatchService(
      InkuireDb(
        Seq(
          mainExternalSignature
        ),
        Map.empty
      )
    )
    //when
    val res: Seq[ExternalSignature] = exactMatchService |??| ResolveResult(Seq(mainSignature))

    //then
    res should matchTo[Seq[ExternalSignature]](Seq(mainExternalSignature))
  }
}

object ExactMatchServiceTest {
  trait Fixture {

    val equalsSignature: Signature = Signature(
      "Int".concreteType.some,
      Seq(
        "Int".concreteType
      ),
      "Boolean".concreteType,
      SignatureContext.empty
    )
    val equalsExternalSignature: ExternalSignature = ExternalSignature(
      equalsSignature,
      "equals",
      "/Int/equals"
    )

    val toStringSignature: Signature = Signature(
      "Int".concreteType.some,
      Seq.empty,
      "String".concreteType,
      SignatureContext.empty
    )
    val toStringExternalSignature: ExternalSignature = ExternalSignature(
      toStringSignature,
      "toString",
      "/Int/toString"
    )

    val rangeSignature: Signature = Signature(
      "Long".concreteType.some,
      Seq(
        "Long".concreteType
      ),
      GenericType(
        "List".concreteType,
        Seq(
          UnresolvedVariance(
            "Long".concreteType
          )
        )
      ),
      SignatureContext.empty
    )
    val rangeExternalSignature: ExternalSignature = ExternalSignature(
      rangeSignature,
      "range",
      "/Long/range"
    )

    val mainSignature: Signature = Signature(
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
    val mainExternalSignature: ExternalSignature = ExternalSignature(
      mainSignature,
      "main",
      "/Main/main"
    )
  }
}
