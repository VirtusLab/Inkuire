package org.virtuslab.inkuire.engine

import org.virtuslab.inkuire.engine.cli.Cli
import org.virtuslab.inkuire.engine.model.Engine.Env
import org.virtuslab.inkuire.engine.model.InkuireDb
import org.virtuslab.inkuire.engine.service.ExactMatchService

object Main extends App {

  val in = new Cli
  val out = new Cli
  val matchService = (db: InkuireDb) => new ExactMatchService(db)

  in
    .readInput(args)
    .toOption
    .semiflatMap { db: InkuireDb =>
      out
        .serveOutput
        .runA(
          Env(db, matchService(db))
        )
    }
    .fold(println("Unexpected Error occured!"))(identity)
    .unsafeRunSync()

}
