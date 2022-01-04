package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model.ExternalSignature
import org.virtuslab.inkuire.engine.common.model.InkuireDb
import org.virtuslab.inkuire.engine.common.model.ResolveResult
import org.virtuslab.inkuire.engine.common.model.Signature

trait BaseMatchService {
  def inkuireDb: InkuireDb
  def findMatches(resolveResult: ResolveResult): Seq[(ExternalSignature, Signature)]
  def isMatch(resolveResult:     ResolveResult)(against: ExternalSignature): Option[Signature]
}
