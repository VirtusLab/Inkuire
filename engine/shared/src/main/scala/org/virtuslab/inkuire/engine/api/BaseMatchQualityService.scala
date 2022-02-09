package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.impl.model._

trait BaseMatchQualityService {
  def sortMatches(functions: Seq[(AnnotatedSignature, Signature)]): Seq[(AnnotatedSignature, Int)] =
    functions
      .map {
        case (fun, matching) => fun -> matchQualityMetric(fun, matching)
      }
      .sortBy(_._2)

  def matchQualityMetric(AnnotatedSignature: AnnotatedSignature, matching: Signature): Int
}
