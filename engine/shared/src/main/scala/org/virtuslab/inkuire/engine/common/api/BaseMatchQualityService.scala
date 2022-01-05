package org.virtuslab.inkuire.engine.common.api

import org.virtuslab.inkuire.engine.common.model._

trait BaseMatchQualityService {

  def sortMatches(functions: Seq[(ExternalSignature, Signature)]): Seq[(ExternalSignature, Int)] =
    functions
      .map {
        case (fun, matching) => fun -> matchQualityMetric(fun, matching)
      }
      .sortBy(_._2)

  def matchQualityMetric(externalSignature: ExternalSignature, matching: Signature): Int

}
