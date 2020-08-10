package org.virtuslab.inkuire.engine.service

import org.virtuslab.inkuire.engine.model.ExternalSignature

trait SignaturePrettifier {
  def prettify(sgns: Seq[ExternalSignature]): String
}
