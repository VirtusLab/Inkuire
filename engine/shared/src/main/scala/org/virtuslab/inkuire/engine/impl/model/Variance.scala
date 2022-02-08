package org.virtuslab.inkuire.engine.impl.model

sealed abstract class Variance {
  val typ: TypeLike
}

/**
 * Java Klass<? extends Param>
 * Kotlin Klass<out Param>
 * Scala Klass[+Param]
 */
case class Covariance(typ: TypeLike) extends Variance

/**
 * Java Klass<? super Param>
 * Kotlin Klass<in Param>
 * Scala Klass[-Param]
 */
case class Contravariance(typ: TypeLike) extends Variance

/**
 * Java Klass<Param>
 * Kotlin Klass<Param>
 * Scala Klass[Param]
 */
case class Invariance(typ: TypeLike) extends Variance

/**
 * Variance of `types` from queries
 */
case class UnresolvedVariance(typ: TypeLike) extends Variance
