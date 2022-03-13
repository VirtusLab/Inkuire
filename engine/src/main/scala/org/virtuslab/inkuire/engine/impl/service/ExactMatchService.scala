package org.virtuslab.inkuire.engine.impl.service

import org.virtuslab.inkuire.engine.api._
import org.virtuslab.inkuire.engine.impl.model._

class ExactMatchService(val inkuireDb: InkuireDb) extends BaseMatchService {

  override def findMatches(resolveResult: ResolveResult): Seq[(AnnotatedSignature, Signature)] = {
    inkuireDb.functions
      .map(fun => fun -> isMatch(resolveResult)(fun))
      .collect {
        case (fun, Some(matching)) => fun -> matching
      }
  }

  override def isMatch(resolveResult: ResolveResult)(against: AnnotatedSignature): Option[Signature] =
    resolveResult.signatures.collectFirst {
      case s if s == against.signature => s
    }
}
