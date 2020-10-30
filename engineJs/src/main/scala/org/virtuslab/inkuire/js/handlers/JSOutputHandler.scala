package org.virtuslab.inkuire.js.handlers

import cats.effect.IO
import monix.execution.Ack
import monix.reactive.subjects.PublishSubject
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.virtuslab.inkuire.engine.common.api.OutputHandler
import org.virtuslab.inkuire.engine.common.model.{Engine, Signature}
import org.virtuslab.inkuire.js.html.{BaseInput, BaseOutput, InputElement}

class JSOutputHandler extends OutputHandler {
  private val subject = PublishSubject[Observable[String]]()
  private val input:     InputElement = InputElement()
  private val inputApi:  BaseInput    = input
  private val outputApi: BaseOutput   = input

  override def serveOutput(env: Engine.Env): IO[Unit] = {
    def executeQuery(query: String): Either[String, Observable[String]] = {
      env.parser
        .parse(query)
        .map(env.resolver.resolve)
        .map { r =>
          Observable
            .fromIterable(env.db.functions)
            .filterEvalF(eSgn => IO.async[Boolean](_(Right(env.matcher.|?|(r)(eSgn)))))
            .map(eSgn => env.prettifier.prettify(Seq(eSgn)))
        }
    }

    IO.async(_ => {

      outputApi.registerOutput(subject)
      inputApi.inputChanges.map(executeQuery).subscribe {
        case Right(v) =>
          subject.onNext(v)
          Ack.Continue
        case Left(v) =>
          println(s"From output: $v")
          Ack.Continue
      }
    })
  }
}
