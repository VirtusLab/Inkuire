package org.virtuslab.inkuire.engine.service

import org.virtuslab.inkuire.engine.model.{DRI, ExternalSignature, Type}
import org.virtuslab.inkuire.model.{SDFunction, SDRI, SProjection}

trait DokkaModelTranslationService {
  def translateFunction(f: SDFunction): List[ExternalSignature]

  def translateProjection(projection: SProjection): Type

  def translateDRI(SDRI: SDRI): DRI
}
