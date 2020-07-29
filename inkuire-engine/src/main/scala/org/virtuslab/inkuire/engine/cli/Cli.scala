package org.virtuslab.inkuire.engine.cli

import cats.Id
import cats.data.{EitherT, StateT}
import cats.instances.all._
import cats.syntax.all._
import cats.effect.IO
import org.virtuslab.inkuire.engine.api.{InputHandler, OutputHandler}
import org.virtuslab.inkuire.engine.utils.syntax._
import org.virtuslab.inkuire.engine.cli.model.{CliContext, CliParam}
import org.virtuslab.inkuire.engine.model.InkuireDb
import org.virtuslab.inkuire.engine.model.Engine._
import org.virtuslab.inkuire.engine.cli.service.KotlinExternalSignaturePrettifier
import org.virtuslab.inkuire.engine.parser.KotlinSignatureParser
import org.virtuslab.inkuire.engine.utils.helpers.IOHelpers

import scala.io.StdIn.readLine
import scala.annotation.tailrec

class Cli extends InputHandler with OutputHandler with IOHelpers {

  @tailrec
  private def parseArgs(args: List[String], agg: Either[String, List[CliParam]] = List.empty.right): Either[String, List[CliParam]] = {
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

  private def handleSyntaxError(err: String): Engine[Unit] = {
    IO {
      println("Syntax error:")
      println(err)
    }.liftApp
  }

  private def handleCommand(input: String): Engine[Unit] = {
    StateT.get[IO, Env] >>= { env =>
      KotlinSignatureParser
        .parse(input)
        .fold(
          handleSyntaxError,
          s => printlnIO(KotlinExternalSignaturePrettifier.prettify(env.matcher |??| s)).liftApp
        )
    }
  }

  def serveOutput: Engine[Unit] = {
    IO {
      print(s"inkuire> ")
      readLine()
    }.liftApp >>= { command: String =>
      if (command.toLowerCase == "exit") {
        IO { println("bye") }.liftApp
      } else {
        handleCommand(command) >>
          serveOutput
      }
    }
  }

  def readInput(args: Seq[String]): EitherT[IO, String, InkuireDb] = {
    parseArgs(args.toList)
      .map(CliContext.create)
      .traverse { context =>
        IO {
          InkuireDb.readFromPath(context.dbPath)
        }
      }
      .pure[Id]
      .fmap(new EitherT(_))
  }

}