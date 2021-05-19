package org.virtuslab.inkuire.engine.common.model

import cats.implicits.{catsSyntaxOptionId, toBifunctorOps, toShow, toTraverseOps}
import io.circe._
import io.circe.parser._
import io.circe.generic.auto._
import io.circe.syntax._
import com.softwaremill.quicklens._

case class InkuireDb(
  functions: Seq[ExternalSignature],
  types:     Map[ITID, (Type, Seq[Type])]
)
