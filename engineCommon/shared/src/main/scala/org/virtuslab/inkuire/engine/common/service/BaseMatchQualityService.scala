package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model._

trait BaseMatchQualityService {

  def sortMatches(functions: Seq[(ExternalSignature, Signature)]): Seq[ExternalSignature]

  def matchQualityMetric(externalSignature: ExternalSignature, matching: Signature): Int

}