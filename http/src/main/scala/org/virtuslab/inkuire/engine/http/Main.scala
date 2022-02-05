package org.virtuslab.inkuire.engine.http

import org.virtuslab.inkuire.engine.common.api.InkuireRunner
import org.virtuslab.inkuire.engine.http.cli.Cli
import org.virtuslab.inkuire.engine.http.http.HttpServer
import scala.concurrent.ExecutionContext

object Main extends App {

  implicit val ec: ExecutionContext = ExecutionContext.global

  InkuireRunner
    .scalaRunner(
      configReader = new Cli,
      inputHandler = new Cli,
      outputHandler = new HttpServer
    )
    .run(args)
    .isCompleted

}
