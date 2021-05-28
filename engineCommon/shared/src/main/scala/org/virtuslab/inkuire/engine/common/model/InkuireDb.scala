package org.virtuslab.inkuire.engine.common.model

import cats.implicits.{catsSyntaxOptionId, toBifunctorOps, toShow, toTraverseOps}
import io.circe._
import io.circe.parser._
import io.circe.generic.auto._
import io.circe.syntax._
import com.softwaremill.quicklens._
import cats.kernel.Monoid

case class InkuireDb(
  functions: Seq[ExternalSignature],
  types:     Map[ITID, (Type, Seq[Type])]
)

object InkuireDb {
  implicit val inkuireDbMonoid = new Monoid[InkuireDb] {
    override def combine(x: InkuireDb, y: InkuireDb): InkuireDb =
      InkuireDb(
        functions = (x.functions ++ y.functions).distinct,
        types = x.types ++ y.types
      )

    override def empty: InkuireDb = InkuireDb(Seq.empty, Map.empty)
  }
}