package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model.{ExternalSignature, InkuireDb, Signature}
import org.virtuslab.inkuire.engine.common.model.ExternalSignature

trait BaseMatchService {
  def inkuireDb: InkuireDb
  def |??|(signature: Signature): Seq[ExternalSignature]
}
