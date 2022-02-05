package org.virtuslab.inkuire.engine.common.utils

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

object fp {

  trait Functor[F[_]] {
    def fmap[A, B](fa: F[A])(f: A => B): F[B]
  }

  trait Monad[F[_]] extends Functor[F] {
    def pure[A](a: A): F[A]
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  }

  case class EitherT[F[_], L, A](value: F[Either[L, A]]) {
    def fmap[B](f: A => B)(implicit fmonad: Monad[F]): EitherT[F, L, B] = new EitherT(value.fmap(_.map(f)))
    def flatMap[B](f: A => EitherT[F, L, B])(implicit fmonad: Monad[F]): EitherT[F, L, B] =
      new EitherT(
        value.flatMap {
          case Left(value) => fmonad.pure(Left(value))
          case Right(value) => f(value).value
        }
      )
    def semiflatmap[B](f: A => Either[L, B])(implicit fmonad: Monad[F]): EitherT[F, L, B] =
      new EitherT(value.fmap(_.flatMap(f)))
    def mapInner[B](f: Either[L, A] => Either[L, B])(implicit fmonad: Monad[F]): EitherT[F, L, B] =
      new EitherT(value.fmap(f))
  }

  implicit class MonadOps[F[_], A](fa: F[A]) {
    def fmap[B](f: A => B)(implicit monad: Monad[F]): F[B] = monad.fmap(fa)(f)
    def flatMap[B](f: A => F[B])(implicit monad: Monad[F]): F[B] = monad.flatMap(fa)(f)
  }

  object Monad {
    def pure[F[_], A](a: A)(implicit monad: Monad[F]) = monad.pure[A](a)
  }

  implicit val optionMonad: Monad[Option] = new Monad[Option] {
    def fmap[A, B](fa: Option[A])(f: A => B): Option[B] = fa.map(f)
    def pure[A](a: A): Option[A] = Some(a)
    def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] = fa.flatMap(f)
  }
  
  implicit def eitherMonad[L] = {
    type EitherL[R] = Either[L, R]
    new Monad[EitherL] {
      def fmap[A, B](fa: Either[L, A])(f: A => B): Either[L, B] = fa.map(f)
      def pure[A](a: A): Either[L, A] = Right(a)
      def flatMap[A, B](fa: Either[L, A])(f: A => Either[L, B]): Either[L, B] = fa.flatMap(f)
    }
  }

  implicit def futureMonad(implicit ec: ExecutionContext) = new Monad[Future] {
    def fmap[A, B](fa: Future[A])(f: A => B): Future[B] = fa.map(f)
    def pure[A](a: A): Future[A] = Future(a)
    def flatMap[A, B](fa: Future[A])(f: A => Future[B]): Future[B] = fa.flatMap(f)
  }

  // implicit def eitherTMonad[F[_], L](implicit fmonad: Monad[F]) = {
  //   type EitherTFL[R] = EitherT[F, L, R]
  //   new Monad[EitherTFL] {
  //     def fmap[A, B](fa: EitherT[F, L, A])(f: A => B): EitherT[F, L, B] = new EitherT(fa.value.fmap(_.map(f)))
  //     def pure[A](a: A): EitherT[F, L, A] = new EitherT(fmonad.pure(Right(a)))
  //     def flatMap[A, B](fa: EitherT[F, L, A])(f: A => EitherT[F, L, B]): EitherT[F, L, B] =
  //       new EitherT(
  //         fa.value.flatMap {
  //           case Left(value) => fmonad.pure(Left(value))
  //           case Right(value) => f(value).value
  //         }
  //       )
  //   }
  // }

  trait Monoid[A] {
    def mempty: A
    def mappend(a: A, b: A): A
  }

  object Monoid {
    def combineAll[A](list: List[A])(implicit monoid: Monoid[A]) =
      list.foldLeft(monoid.mempty) { case (acc, a) => monoid.mappend(acc, a) }
  }

}
