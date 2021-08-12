package org.virtuslab.inkuire.js.handlers

import cats.effect.IO
import monix.execution.Ack
import monix.reactive.subjects.PublishSubject
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import org.virtuslab.inkuire.engine.common.api.OutputHandler
import org.virtuslab.inkuire.engine.common.model.{Engine, Signature}
import org.virtuslab.inkuire.engine.http.http.OutputFormatter
import org.virtuslab.inkuire.js.worker.JSHandler
import org.virtuslab.inkuire.engine.common.model.OutputFormat
import org.virtuslab.inkuire.engine.common.model.EndFormat

class JSOutputHandler(private val jsHandler: JSHandler) extends OutputHandler {
  private val subject = PublishSubject[Observable[OutputFormat]]()

  override def serveOutput(env: Engine.Env): IO[Unit] = {
    val outputFormatter = new OutputFormatter(env.prettifier)

    def executeQuery(query: String): Either[String, Observable[OutputFormat]] = {
      env.parser
        .parse(query)
        .flatMap(env.resolver.resolve)
        .map { r =>
          Observable
            .fromIterable(env.db.functions)
            .filterEvalF { eSgn =>
              IO.async[Boolean] { f =>
                f(Right(env.matcher.isMatch(r)(eSgn)))
              }
            }
            .map(eSgn => outputFormatter.createOutput(query, Seq(eSgn)))
            .:+(EndFormat)
        }
    }

    IO.async { _ =>
      jsHandler.registerOutput(subject)
      jsHandler.inputChanges.map(executeQuery).subscribe {
        case Right(v) =>
          subject.onNext(v)
          Ack.Continue
        case Left(v) =>
          jsHandler.handleQueryEnded(v)
          Ack.Continue
      }
      jsHandler.notifyEngineReady
    }
  }
}
