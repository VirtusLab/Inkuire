package org.virtuslab.inkuire.engine.common.api

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

case class FutureExcept[A](value: Future[Either[String, A]]) {
  def fmap[B](f: A => B)(implicit ec: ExecutionContext): FutureExcept[B] = new FutureExcept(value.map(_.map(f)))
  def flatMap[B](f: A => FutureExcept[B])(implicit ec: ExecutionContext): FutureExcept[B] =
    new FutureExcept(
      value.flatMap {
        case Left(value) => Future(Left(value))
        case Right(value) => f(value).value
      }
    )
  def semiflatmap[B](f: A => Either[String, B])(implicit ec: ExecutionContext): FutureExcept[B] =
    new FutureExcept(value.map(_.flatMap(f)))
  def mapInner[B](f: Either[String, A] => Either[String, B])(implicit ec: ExecutionContext): FutureExcept[B] =
    new FutureExcept(value.map(f))
}