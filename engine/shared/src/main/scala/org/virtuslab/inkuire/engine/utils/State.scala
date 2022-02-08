package org.virtuslab.inkuire.engine.utils

case class State[S, A](runState: S => (S, A)) {
  def map[B](f: A => B): State[S, B] = State { (s0: S) =>
    val (s, a) = runState(s0)
    (s, f(a))
  }

  def flatMap[B](f: A => State[S, B]): State[S, B] = State { (s0: S) =>
    val (s, a) = runState(s0)
    f(a).runState(s)
  }

  def evalState: S => A = { (s: S) => runState(s)._2 }

  def execState: S => S = { (s: S) => runState(s)._1 }

  def >>[B](next: State[S, B]): State[S, B] = this.flatMap(_ => next)
}

object State {

  def pure[S, A](a: A): State[S, A] = State(s => (s, a))

  def get[S]: State[S, S] = State(s => (s, s))

  def put[S](s: S): State[S, Unit] = State(_ => (s, ()))

  def modify[S](f: S => S): State[S, Unit] = State(s => (f(s), ()))
}
