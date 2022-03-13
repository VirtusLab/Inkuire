package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.impl.model.AnnotatedSignature

trait BaseSignaturePrettifier {
  def prettify(sgns: Seq[AnnotatedSignature]): String
}
