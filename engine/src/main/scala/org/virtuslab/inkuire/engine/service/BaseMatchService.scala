package org.virtuslab.inkuire.engine.service

import org.virtuslab.inkuire.engine.model.{ExternalSignature, InkuireDb, Signature}

trait BaseMatchService {
  def inkuireDb: InkuireDb
  def |??|(signature: Signature): Seq[ExternalSignature]
}
