package org.virtuslab.inkuire.engine.common.utils.helpers

import cats.effect.IO

trait IOHelpers {
  def putStrLn(str: String): IO[Unit] = IO { println(str) }
  def putStr(str:   String): IO[Unit] = IO { print(str) }
}
