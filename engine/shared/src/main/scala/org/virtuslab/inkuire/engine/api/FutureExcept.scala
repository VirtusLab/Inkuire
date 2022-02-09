package org.virtuslab.inkuire.engine.api

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case class FutureExcept[A](value: Future[Either[String, A]]) {
  def map[B](f: A => B)(implicit ec: ExecutionContext): FutureExcept[B] =
    new FutureExcept(value.map(_.map(f)))

  def flatMap[B](f: A => FutureExcept[B])(implicit ec: ExecutionContext): FutureExcept[B] =
    new FutureExcept(
      value.flatMap {
        case Left(value)  => Future(Left(value))
        case Right(value) => f(value).value
      }
    )

  def semiflatmap[B](f: A => Either[String, B])(implicit ec: ExecutionContext): FutureExcept[B] =
    new FutureExcept(value.map(_.flatMap(f)))

  def mapInner[B](f: Either[String, A] => Either[String, B])(implicit ec: ExecutionContext): FutureExcept[B] =
    new FutureExcept(value.map(f))
}

object FutureExcept {
  def pure[A](a: A)(implicit ec: ExecutionContext): FutureExcept[A] =
    FutureExcept(Future(Right(a)))

  def fromExcept[A](e: Either[String, A])(implicit ec: ExecutionContext): FutureExcept[A] =
    FutureExcept(Future(e))

  def fromFuture[A](f: Future[A])(implicit ec: ExecutionContext): FutureExcept[A] =
    FutureExcept(f.map(Right.apply[String, A]))
}
