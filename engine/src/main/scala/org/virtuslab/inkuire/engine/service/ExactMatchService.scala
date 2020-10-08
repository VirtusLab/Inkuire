package org.virtuslab.inkuire.engine.service

import org.virtuslab.inkuire.engine.model.{ExternalSignature, InkuireDb, Signature}

class ExactMatchService(val inkuireDb: InkuireDb) extends BaseMatchService {

  override def |??|(signature: Signature): Seq[ExternalSignature] = {
    inkuireDb.functions.filter(_.signature == signature)
  }
}
