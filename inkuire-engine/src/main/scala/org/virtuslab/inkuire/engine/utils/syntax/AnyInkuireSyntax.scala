package org.virtuslab.inkuire.engine.utils.syntax

trait AnyInkuireSyntax {
  implicit class AnyInkuireSyntax[A](v: A) {
    def some: Option[A] = Some(v)
    def right[L]: Either[L, A] = Right(v)
    def left[R]: Either[A, R] = Left(v)
    def whenOrElse(pred: Boolean)(msg: String): Either[String, A] = Option.when(pred)(v).toRight(msg)
  }
}
