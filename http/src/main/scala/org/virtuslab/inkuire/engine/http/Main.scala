package org.virtuslab.inkuire.engine.http

import org.virtuslab.inkuire.engine.common.api.InkuireRunner
import org.virtuslab.inkuire.engine.http.cli.Cli
import org.virtuslab.inkuire.engine.http.http.HttpServer

object Main extends App {

  InkuireRunner
    .scalaRunner(
      configReader = new Cli,
      inputHandler = new Cli,
      outputHandler = new HttpServer
    )
    .run(args)
    .unsafeRunSync()

}
