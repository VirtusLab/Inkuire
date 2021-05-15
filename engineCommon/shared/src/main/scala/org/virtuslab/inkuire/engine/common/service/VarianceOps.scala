package org.virtuslab.inkuire.engine.common.service

import org.virtuslab.inkuire.engine.common.model._

trait VarianceOps {
  implicit class TypeVarianceOps(typ: Type) {
    def zipVariance(v: Variance): Variance = v match {
      case _: Contravariance => Contravariance(typ)
      case _: Covariance     => Covariance(typ)
      case _: Invariance     => Invariance(typ)
    }
  }
}