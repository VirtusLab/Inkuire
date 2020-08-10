package org.virtuslab.inkuire.engine.service

import org.virtuslab.inkuire.engine.model.{ExternalSignature, Type}
import org.virtuslab.inkuire.model.{SDFunction, SDRI}

trait DokkaModelTranslationService {
  def translateFunction(f: SDFunction): ExternalSignature

  def translateTypeBound(dri: SDRI): Type
}
