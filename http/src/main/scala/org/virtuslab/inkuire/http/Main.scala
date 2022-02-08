package org.virtuslab.inkuire.http

import org.virtuslab.inkuire.engine.api.InkuireRunner
import org.virtuslab.inkuire.http.Cli
import org.virtuslab.inkuire.http.HttpServer

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

object Main extends App {

  implicit val ec: ExecutionContext = ExecutionContext.global

  Await.ready(
    InkuireRunner
      .scalaRunner(
        configReader = new Cli,
        inputHandler = new Cli,
        outputHandler = new HttpServer
      )
      .run(args.toSeq),
    Duration.Inf
  )

}
