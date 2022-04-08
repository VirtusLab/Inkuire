package org.virtuslab.inkuire.http

import org.virtuslab.inkuire.engine.api.InkuireRunner
import org.virtuslab.inkuire.engine.api.InkuireEnv
import org.virtuslab.inkuire.http.Cli
import org.virtuslab.inkuire.http.HttpServer

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.util.chaining._
import scala.concurrent.Future

object Main {
  def main(args: Array[String]): Unit = {

    implicit val ec: ExecutionContext = ExecutionContext.global

    val cli = new Cli

    val appConfig = cli.readConfig(args.toSeq).fold(_ => AppConfig.empty, identity)

    cli
      .readInput(args.toSeq)
      .map(_.map(InkuireEnv.defaultScalaEnv()))
      .flatMap {
        case Right(env) =>
          new HttpServer(appConfig).serveOutput(env)
        case _ =>
          Future(())
      }
      .pipe { action =>
        Await.ready(
          action,
          Duration.Inf
        )
      }

  }
}
