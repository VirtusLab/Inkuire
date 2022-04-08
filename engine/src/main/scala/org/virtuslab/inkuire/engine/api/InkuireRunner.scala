package org.virtuslab.inkuire.engine.api

import org.virtuslab.inkuire.engine.impl.service._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.chaining._

class InkuireRunner(
  val inputHandler:  InputHandler,
  val outputHandler: OutputHandler,
  val inkuireEnvGen: InkuireDb => InkuireEnv
) {
  def run(args: Seq[String])(implicit ec: ExecutionContext): Future[Unit] =
    inputHandler
      .readInput(args)
      .pipe(FutureExcept.apply)
      .flatMap { (db: InkuireDb) =>
        outputHandler
          .serveOutput(
            inkuireEnvGen(db)
          )
          .pipe(FutureExcept.fromFuture)
      }
      .value
      .map(
        _.fold(
          str => println(s"Oooooh man, bad luck. Inkuire encountered an unexpected error. Caused by $str"),
          identity
        )
      )

}

object InkuireRunner {
  def fromHandlers(
    inputHandler:  InputHandler,
    outputHandler: OutputHandler,
    inkuireEnvGen: InkuireDb => InkuireEnv
  ): InkuireRunner = {
    new InkuireRunner(
      inputHandler,
      outputHandler,
      inkuireEnvGen
    )
  }
}
