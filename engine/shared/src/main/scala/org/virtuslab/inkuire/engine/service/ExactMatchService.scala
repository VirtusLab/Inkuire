package org.virtuslab.inkuire.engine.service

import org.virtuslab.inkuire.engine.api._
import org.virtuslab.inkuire.engine.model._

class ExactMatchService(val inkuireDb: InkuireDb) extends BaseMatchService {

  override def findMatches(resolveResult: ResolveResult): Seq[(ExternalSignature, Signature)] = {
    inkuireDb.functions
      .map(fun => fun -> isMatch(resolveResult)(fun))
      .collect {
        case (fun, Some(matching)) => fun -> matching
      }
  }

  override def isMatch(resolveResult: ResolveResult)(against: ExternalSignature): Option[Signature] =
    resolveResult.signatures.collectFirst {
      case s if s == against.signature => s
    }
}
