package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.impl.model.AnnotatedSignature

trait BaseSignaturePrettifier {
  def prettifyAll(sgns: Seq[AnnotatedSignature]): String
  def prettify(esgn:    AnnotatedSignature):      String
}
