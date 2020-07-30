package org.virtuslab.inkuire.engine.cli

import java.nio.file.Paths

import cats.data.StateT
import cats.instances.all._
import cats.syntax.all._
import cats.effect.IO
import org.virtuslab.inkuire.engine.utils.syntax._
import org.virtuslab.inkuire.engine.cli.model.{CliContext, CliParam}
import org.virtuslab.inkuire.engine.model.InkuireDb
import org.virtuslab.inkuire.engine.cli.model.Engine._
import org.virtuslab.inkuire.engine.cli.service.KotlinExternalSignaturePrettifier
import org.virtuslab.inkuire.engine.parser.KotlinSignatureParser
import org.virtuslab.inkuire.engine.service.ExactMatchService
import org.virtuslab.inkuire.engine.utils.helpers.IOHelpers

import scala.io.StdIn.readLine
import scala.annotation.tailrec

object Main extends App with IOHelpers {

  @tailrec
  def parseArgs(args: List[String], agg: Either[String, List[CliParam]] = List.empty.right): Either[String, List[CliParam]] = {
    args match {
      case Nil => agg
      case opt :: v :: tail =>
        parseArgs(
          tail,
          agg >>= { list =>
            CliParam
              .parseCliOption(opt, v)
              .fmap(list :+ _)
          }
        )
      case arg :: Nil =>
        s"Wrong argument $arg".left
    }
  }

  def handleSyntaxError(err: String): Engine[Unit] = {
    IO {
      println("Syntax error:")
      println(err)
    }.liftApp
  }

  def handleCommand(input: String): Engine[Unit] = {
    StateT.get[IO, Env] >>= { env =>
      KotlinSignatureParser
        .parse(input)
        .fold(
          handleSyntaxError,
          s => printlnIO(KotlinExternalSignaturePrettifier.prettify(env.matcher |??| s)).liftApp
        )
    }
  }

  def handleCommands: Engine[Unit] = {
    IO {
      print(s"inkuire> ")
      readLine()
    }.liftApp >>= { command: String =>
      if (command.toLowerCase == "exit") {
        IO { println("bye") }.liftApp
      } else {
        handleCommand(command) >>
          handleCommands
      }
    }
  }

  def startConsole(data: CliContext): IO[Unit] =
    IO { InkuireDb.readFromPath(data.dbPath) } >>= { db =>
      handleCommands.runA(Env(db, data.dbPath, new ExactMatchService(db)))
    }

  val cli = parseArgs(args.toList).map(CliContext.create).fold(printlnIO, startConsole)

  cli.unsafeRunSync()
}