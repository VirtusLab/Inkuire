package org.virtuslab.inkuire.engine.model

sealed class Variance(val typ: Type)

// Java Klass<? extends Param>
// Kotlin Klass<out Param>
// Scala Klass[+Param]
case class Covariance(_typ: Type) extends Variance(_typ)

// Java Klass<? super Param>
// Kotlin Klass<in Param>
// Scala Klass[-Param]
case class Contravariance(_typ: Type) extends Variance(_typ)

// Java Klass<Param>
// Kotlin Klass<Param>
// Scala Klass[Param]
case class Invariance(_typ: Type) extends Variance(_typ)

// Type for `types` from queries
case class UnresolvedVariance(_typ: Type) extends Variance(_typ)