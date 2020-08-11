package org.virtuslab.inkuire.engine.service

import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.BaseInkuireTest
import org.virtuslab.inkuire.engine.model.{ExternalSignature, GenericType, InkuireDb, Signature, SignatureContext}
import org.virtuslab.inkuire.engine.model.Type._
import org.virtuslab.inkuire.engine.service.ExactMatchServiceTest.Fixture

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
    val res: Seq[ExternalSignature] = exactMatchService |??| equalsSignature

    //then
    res should matchTo[Seq[ExternalSignature]] (Seq(equalsExternalSignature))
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
    val res: Seq[ExternalSignature] = exactMatchService |??| toStringSignature

    //then
    res should matchTo[Seq[ExternalSignature]] (Seq.empty)
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
    val res: Seq[ExternalSignature] = exactMatchService |??| rangeSignature

    //then
    res should matchTo[Seq[ExternalSignature]] (Seq(rangeExternalSignature))
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
    val res: Seq[ExternalSignature] = exactMatchService |??| mainSignature

    //then
    res should matchTo[Seq[ExternalSignature]] (Seq(mainExternalSignature))
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
          "Long".concreteType
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
            "String".concreteType
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
