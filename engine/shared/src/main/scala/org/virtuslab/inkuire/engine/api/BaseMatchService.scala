package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.impl.model.AnnotatedSignature
import org.virtuslab.inkuire.engine.impl.model.ResolveResult
import org.virtuslab.inkuire.engine.impl.model.Signature
import org.virtuslab.inkuire.engine.api.InkuireDb

trait BaseMatchService {
  def inkuireDb: InkuireDb
  def findMatches(resolveResult: ResolveResult): Seq[(AnnotatedSignature, Signature)]
  def isMatch(resolveResult:     ResolveResult)(against: AnnotatedSignature): Option[Signature]
}
