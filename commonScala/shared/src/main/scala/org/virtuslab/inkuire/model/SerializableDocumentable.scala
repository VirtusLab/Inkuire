package org.virtuslab.inkuire.model

case class SDRI(
  packageName:  Option[String],
  className:    Option[String],
  callableName: Option[String],
  original:     String
) {
  override def toString: String = original
}

case class SDFunction(
  dri:                  SDRI,
  name:                 String,
  isConstructor:        Boolean,
  parameters:           List[SDParameter],
  areParametersDefault: List[Boolean],
  `type`:               SBound,
  generics:             List[SDTypeParameter],
  receiver:             Option[SDParameter],
  location:             String
)

case class SDParameter(
  dri:    SDRI,
  name:   Option[String],
  `type`: SBound
)

case class SDTypeParameter(
  variantTypeParameter: SVariance,
  bounds:               List[SBound]
)

case class AncestryGraph(
  dri:        SDRI,
  `type`:     SBound,
  superTypes: List[SProjection]
)
