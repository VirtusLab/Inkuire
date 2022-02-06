package org.virtuslab.inkuire.engine.common.utils

trait Monoid[A] {
  def mempty: A
  def mappend(a: A, b: A): A
}

object Monoid {

  def apply[A](implicit monoid: Monoid[A]): Monoid[A] = monoid

  def combineAll[A](list: List[A])(implicit monoid: Monoid[A]): A =
    list.foldLeft[A](monoid.mempty) {
      case (acc, a) => monoid.mappend(acc, a)
    }
}
