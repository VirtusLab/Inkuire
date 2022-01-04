package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model.ExternalSignature

trait SignaturePrettifier {
  def prettify(sgns: Seq[ExternalSignature]): String
}
