package org.virtuslab.inkuire.engine.utils.helpers

import cats.effect.IO

trait IOHelpers {
  def printlnIO(str: String): IO[Unit] = IO { println(str) }
  def printIO(str: String): IO[Unit] = IO { print(str) }
}
