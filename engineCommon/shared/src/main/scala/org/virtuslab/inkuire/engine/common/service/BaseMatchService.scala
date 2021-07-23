package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model.{ExternalSignature, InkuireDb, ResolveResult, Signature}

trait BaseMatchService {
  def inkuireDb: InkuireDb
  def findMatches(resolveResult: ResolveResult): Seq[ExternalSignature]
  def isMatch(resolveResult:     ResolveResult)(against: ExternalSignature): Boolean
}
