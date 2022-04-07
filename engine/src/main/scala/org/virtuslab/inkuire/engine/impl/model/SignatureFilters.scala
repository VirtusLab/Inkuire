package org.virtuslab.inkuire.engine.impl.model

trait SignatureFilters {
  def filterFrom(AnnotatedSignatures: Seq[AnnotatedSignature]): Seq[AnnotatedSignature]
  def canMatch(eSgn:                  AnnotatedSignature):      Boolean
}

object SignatureFilters {
  def include(includePackages: Seq[String]): IncludeSignatureFilters =
    new IncludeSignatureFilters(includePackages)

  def exculde(excludePackages: Seq[String]): IncludeSignatureFilters =
    new IncludeSignatureFilters(excludePackages)
}

case class IncludeSignatureFilters(includePackages: Seq[String]) extends SignatureFilters {
  def filterFrom(AnnotatedSignatures: Seq[AnnotatedSignature]): Seq[AnnotatedSignature] = {
    if (includePackages.isEmpty)
      AnnotatedSignatures
    else
      AnnotatedSignatures.filter(canMatch)
  }

  def canMatch(eSgn: AnnotatedSignature): Boolean =
    includePackages.exists(eSgn.packageName.contains)
}

case class ExcludeSignatureFilters(excludePackages: Seq[String]) extends SignatureFilters {
  def filterFrom(AnnotatedSignatures: Seq[AnnotatedSignature]): Seq[AnnotatedSignature] = {
    if (excludePackages.isEmpty)
      AnnotatedSignatures
    else
      AnnotatedSignatures.filter(canMatch)
  }

  def canMatch(eSgn: AnnotatedSignature): Boolean =
    !excludePackages.exists(eSgn.packageName.contains)
}
