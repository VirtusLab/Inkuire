package org.virtuslab.inkuire.http

import org.virtuslab.inkuire.engine.api.InkuireRunner
import org.virtuslab.inkuire.http.Cli
import org.virtuslab.inkuire.http.HttpServer

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import org.virtuslab.inkuire.engine.impl.model.AppConfig
import org.virtuslab.inkuire.engine.api.FutureExcept

import scala.util.chaining._

object Main extends App {

  implicit val ec: ExecutionContext = ExecutionContext.global

  val cli = new Cli

  Await.ready(
    cli
      .readConfig(args.toSeq)
      .flatMap { (appConfig: AppConfig) =>
        InkuireRunner
          .scalaRunner(
            inputHandler = cli,
            outputHandler = new HttpServer(appConfig)
          )
          .run(args.toSeq)
          .pipe(FutureExcept.fromFuture)
      }.value,
    Duration.Inf
  )

}
