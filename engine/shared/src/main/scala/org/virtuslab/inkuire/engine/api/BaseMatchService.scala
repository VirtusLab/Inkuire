package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.model.ExternalSignature
import org.virtuslab.inkuire.engine.model.InkuireDb
import org.virtuslab.inkuire.engine.model.ResolveResult
import org.virtuslab.inkuire.engine.model.Signature

trait BaseMatchService {
  def inkuireDb: InkuireDb
  def findMatches(resolveResult: ResolveResult): Seq[(ExternalSignature, Signature)]
  def isMatch(resolveResult:     ResolveResult)(against: ExternalSignature): Option[Signature]
}
