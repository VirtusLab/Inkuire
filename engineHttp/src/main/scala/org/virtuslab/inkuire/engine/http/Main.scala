package org.virtuslab.inkuire.engine.http

import org.virtuslab.inkuire.engine.http.cli.Cli
import org.virtuslab.inkuire.engine.common.model.Engine.Env
import org.virtuslab.inkuire.engine.common.model.{AppConfig, InkuireDb}
import org.virtuslab.inkuire.engine.common.parser.KotlinSignatureParserService
import org.virtuslab.inkuire.engine.common.service._
import org.virtuslab.inkuire.engine.http.http.HttpServer
import java.io.File
import java.io.FileWriter
import org.virtuslab.inkuire.engine.common.serialization.EngineModelSerializers

object Main extends App {

  def dumpDB(db: InkuireDb) = {
    val file = new File("/home/kkorban/Inkuire/data/db.json")
    file.createNewFile()
    val myWriter = new FileWriter("/home/kkorban/Inkuire/data/db.json")
    println(db)
    myWriter.write(EngineModelSerializers.serialize(db))
    myWriter.close()
  }

  val configReader = new Cli
  val in           = new Cli
  val out          = new HttpServer
  val matchService = (db: InkuireDb) => new FluffMatchService(db)
  val prettifier   = new KotlinExternalSignaturePrettifier
  val resolver     = (db: InkuireDb) => new DefaultSignatureResolver(db.types)
  val parser       = new KotlinSignatureParserService

  configReader
    .readConfig(args)
    .flatMap { config: AppConfig =>
      in.readInput(config)
        .semiflatMap { db: InkuireDb =>
          out
            .serveOutput()
            .runA(
              Env(db, matchService(db), prettifier, parser, resolver(db), config)
            )
        }
    }
    .fold(str => println(s"Oooooh man, bad luck. Inkuire encountered an unexpected error. Caused by $str"), identity)
    .unsafeRunSync()

}
