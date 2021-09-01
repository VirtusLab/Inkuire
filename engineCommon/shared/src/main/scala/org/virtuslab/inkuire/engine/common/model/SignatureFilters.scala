package org.virtuslab.inkuire.engine.common.model

trait SignatureFilters {
  def filterFrom(externalSignatures: Seq[ExternalSignature]): Seq[ExternalSignature]
  def canMatch(eSgn: ExternalSignature): Boolean
}

object SignatureFilters {
  def include(includePackages: Seq[String]): IncludeSignatureFilters =
    new IncludeSignatureFilters(includePackages)

  def exculde(excludePackages: Seq[String]): IncludeSignatureFilters =
    new IncludeSignatureFilters(excludePackages)
}

case class IncludeSignatureFilters(includePackages: Seq[String]) extends SignatureFilters {
  def filterFrom(externalSignatures: Seq[ExternalSignature]): Seq[ExternalSignature] = {
    if (includePackages.isEmpty)
      externalSignatures
    else
      externalSignatures.filter(canMatch)
  }

  def canMatch(eSgn: ExternalSignature): Boolean =
    includePackages.exists(eSgn.packageName.contains)
}

case class ExcludeSignatureFilters(excludePackages: Seq[String]) extends SignatureFilters {
  def filterFrom(externalSignatures: Seq[ExternalSignature]): Seq[ExternalSignature] = {
    if (excludePackages.isEmpty)
      externalSignatures
    else
      externalSignatures.filter(canMatch)
  }

  def canMatch(eSgn: ExternalSignature): Boolean =
    !excludePackages.exists(eSgn.packageName.contains)
}