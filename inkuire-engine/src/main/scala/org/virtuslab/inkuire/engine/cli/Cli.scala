package org.virtuslab.inkuire.engine.cli

import java.nio.file.Paths

import cats.Id
import cats.data.EitherT
import cats.data.StateT
import cats.instances.all._
import cats.syntax.all._
import cats.effect.IO
import org.virtuslab.inkuire.engine.api.{ConfigReader, InputHandler, OutputHandler}
import org.virtuslab.inkuire.engine.utils.syntax._
import org.virtuslab.inkuire.engine.model.{AppConfig, AppParam, InkuireDb}
import org.virtuslab.inkuire.engine.model.Engine._
import org.virtuslab.inkuire.engine.utils.helpers.IOHelpers

import scala.io.StdIn.readLine
import scala.annotation.tailrec

class Cli extends InputHandler with OutputHandler with ConfigReader with IOHelpers {

  @tailrec
  private def parseArgs[P](f: (String, String) => Either[String, P])(
    args:                     List[String],
    agg:                      Either[String, List[P]] = List.empty.right
  ): Either[String, List[P]] = {
    args match {
      case Nil => agg
      case opt :: v :: tail =>
        parseArgs(f)(
          tail,
          agg >>= { list =>
            f(opt, v)
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
      env.parser
        .parse(input)
        .fold(
          handleSyntaxError,
          s => putStrLn(env.prettifier.prettify(env.matcher |??| s)).liftApp
        )
    }
  }

  override def serveOutput(): Engine[Unit] = {
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

  private def toFile(path: String) = Paths.get(path).toFile

  override def readInput(appConfig: AppConfig): EitherT[IO, String, InkuireDb] = {
    InkuireDb
      .read(
        appConfig.bdPaths.toList.map(path            => toFile(path.path)),
        appConfig.ancestryGraphPaths.toList.map(path => toFile(path.path))
      )
      .traverse(value => IO { value })
      .pure[Id]
      .fmap(new EitherT(_))
  }

  override def readConfig(args: Seq[String]): EitherT[IO, String, AppConfig] = {
    parseArgs(AppParam.parseCliOption)(args.toList)
      .map(AppConfig.create)
      .pure[Id]
      .fmap(config => new EitherT(IO(config)))
  }
}
