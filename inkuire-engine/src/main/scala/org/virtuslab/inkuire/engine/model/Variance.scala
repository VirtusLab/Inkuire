package org.virtuslab.inkuire.engine.model

sealed abstract class Variance {
  val typ: Type
}

// Java Klass<? extends Param>
// Kotlin Klass<out Param>
// Scala Klass[+Param]
case class Covariance(typ: Type) extends Variance

// Java Klass<? super Param>
// Kotlin Klass<in Param>
// Scala Klass[-Param]
case class Contravariance(typ: Type) extends Variance

// Java Klass<Param>
// Kotlin Klass<Param>
// Scala Klass[Param]
case class Invariance(typ: Type) extends Variance

// Type for `types` from queries
case class UnresolvedVariance(typ: Type) extends Variance
