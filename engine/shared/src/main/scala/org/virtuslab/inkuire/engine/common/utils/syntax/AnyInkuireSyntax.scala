package org.virtuslab.inkuire.engine.common.utils.syntax

trait AnyInkuireSyntax {
  implicit class AnyInkuireOps[A](v: A) {
    def right[L]: Either[L, A] = Right(v)
    def left[R]:  Either[A, R] = Left(v)
  }
}
