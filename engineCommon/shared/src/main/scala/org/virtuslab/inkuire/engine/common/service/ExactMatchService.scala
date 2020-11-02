package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model.{ExternalSignature, InkuireDb, ResolveResult, Signature}

class ExactMatchService(val inkuireDb: InkuireDb) extends BaseMatchService {

  override def |??|(resolveResult: ResolveResult): Seq[ExternalSignature] = {
    inkuireDb.functions.filter(|?|(resolveResult))
  }

  override def |?|(resolveResult: ResolveResult)(against: ExternalSignature): Boolean =
    resolveResult.signatures.contains(against.signature)
}
