package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model.{ExternalSignature, InkuireDb, Signature}
import org.virtuslab.inkuire.engine.common.model.ExternalSignature

class ExactMatchService(val inkuireDb: InkuireDb) extends BaseMatchService {

  override def |??|(signature: Signature): Seq[ExternalSignature] = {
    inkuireDb.functions.filter(_.signature == signature)
  }
}
