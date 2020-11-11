package org.virtuslab.inkuire.engine.common.service

import cats.implicits.catsSyntaxOptionId
import org.virtuslab.inkuire.engine.common.model._
import Type.StringTypeOps
import org.virtuslab.inkuire.engine.common.model.ExternalSignature
import FluffMatchServiceTest.Fixture
import org.virtuslab.inkuire.engine.common.BaseInkuireTest

class FluffMatchServiceTest extends BaseInkuireTest {
  it should "match simple function" in new Fixture {
    //given
    val fluffMatchService = new FluffMatchService(
      InkuireDb(
        Seq(
          mapGetSignature
        ),
        ancestryGraph.nodes
      )
    )
    //when
    val res: Seq[ExternalSignature] = fluffMatchService |??| ResolveResult(Seq(mapGetSignature.signature))
    //then
    res should matchTo[Seq[ExternalSignature]](Seq(mapGetSignature))
  }
}

object FluffMatchServiceTest {
  trait Fixture {
    import com.softwaremill.quicklens._
    def createGenericType(
      name:     String,
      vars:     Seq[Variance],
      nullable: Boolean = false,
      isParsed: Boolean
    ): GenericType =
      GenericType(
        name.concreteType
          .modify(_.nullable)
          .setTo(nullable)
          .modify(_.itid)
          .setTo(ITID(name, isParsed).some),
        vars
      )
    def createTypeVariable(name: String, nullable: Boolean = false, isParsed: Boolean): TypeVariable =
      TypeVariable(
        name,
        nullable,
        ITID(name, isParsed).some
      )
    val mapType: Type = createGenericType(
      "Map",
      Seq(
        Invariance(
          createTypeVariable("K", isParsed = false)
        ),
        Covariance(
          createTypeVariable("V", isParsed = false)
        )
      ),
      isParsed = false
    )

    val ancestryGraph: AncestryGraph = AncestryGraph(
      Map(
        mapType.itid.get -> (mapType, Seq.empty)
      )
    )

    val mapGetSignature: ExternalSignature = {
      ExternalSignature(
        Signature(
          mapType.some,
          Seq(
            createTypeVariable("K", isParsed = false)
          ),
          createTypeVariable("V", nullable = true, isParsed = false),
          SignatureContext(
            Set("K", "V"),
            Map.empty
          )
        ),
        "get",
        "uri"
      )
    }
  }
}
