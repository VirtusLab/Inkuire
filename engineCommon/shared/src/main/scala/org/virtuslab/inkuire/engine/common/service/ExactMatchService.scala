package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model.{ExternalSignature, InkuireDb, ResolveResult, Signature}

class ExactMatchService(val inkuireDb: InkuireDb) extends BaseMatchService {

  override def findMatches(resolveResult: ResolveResult): Seq[ExternalSignature] = {
    inkuireDb.functions.filter(isMatch(resolveResult))
  }

  override def isMatch(resolveResult: ResolveResult)(against: ExternalSignature): Boolean =
    resolveResult.signatures.contains(against.signature)
}
