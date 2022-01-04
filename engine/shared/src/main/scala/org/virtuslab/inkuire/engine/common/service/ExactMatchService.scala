package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model.ExternalSignature
import org.virtuslab.inkuire.engine.common.model.InkuireDb
import org.virtuslab.inkuire.engine.common.model.ResolveResult
import org.virtuslab.inkuire.engine.common.model.Signature

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
