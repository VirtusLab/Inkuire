package org.virtuslab.inkuire.engine.common.api

import org.virtuslab.inkuire.engine.common.model.ExternalSignature

trait BaseSignaturePrettifier {
  def prettify(sgns: Seq[ExternalSignature]): String
}
