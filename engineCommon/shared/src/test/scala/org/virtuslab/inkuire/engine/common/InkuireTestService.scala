package org.virtuslab.inkuire.engine.common

import java.net.URL
import scala.io.Source
import java.io.File
import org.virtuslab.inkuire.engine.common.serialization.EngineModelSerializers
import cats.kernel.Monoid
import org.virtuslab.inkuire.engine.common.model.InkuireDb

import scala.util.chaining._
import org.virtuslab.inkuire.engine.common.service.FluffMatchService
import org.virtuslab.inkuire.engine.common.service.DefaultSignatureResolver
import org.virtuslab.inkuire.engine.common.parser.ScalaSignatureParserService
import org.virtuslab.inkuire.engine.common.model.ExternalSignature

class InkuireTestService(path: String) {
  
  private def getURLs(url: URL, filesExtension: String): List[URL] = {
    if (url.toURI.getScheme.toLowerCase == "file" && new File(url.toURI).isDirectory)
      new File(url.toURI).listFiles(_.getName.endsWith(filesExtension)).map(_.toURI.toURL).toList
    else List(url)
  }

  private def getURLContent(url: URL) = Source.fromInputStream(url.openStream()).getLines().mkString

  val db =
    getURLs(new URL(path), ".json")
      .map { file =>
        println(file)
        file
      }
      .map(getURLContent)
      .map(EngineModelSerializers.deserialize)
      .collect {
        case Right(db) => db
      }
      .pipe(Monoid.combineAll[InkuireDb])

  val matchService = new FluffMatchService(db)
  val resolver = new DefaultSignatureResolver(db)
  val parser = new ScalaSignatureParserService

  def query(q: String): Seq[ExternalSignature] = {
    parser
      .parse(q)
      .flatMap(resolver.resolve)
      .map(matchService.findMatches)
      .toOption.toSeq
      .flatMap(_.map(_._1))
  }

}