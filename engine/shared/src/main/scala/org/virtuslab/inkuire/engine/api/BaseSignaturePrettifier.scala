package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.model.ExternalSignature

trait BaseSignaturePrettifier {
  def prettify(sgns: Seq[ExternalSignature]): String
}
