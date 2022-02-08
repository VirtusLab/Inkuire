package org.virtuslab.inkuire.engine

import org.virtuslab.inkuire.engine.model.ExternalSignature
import org.virtuslab.inkuire.engine.model.InkuireDb
import org.virtuslab.inkuire.engine.parser.ScalaSignatureParserService
import org.virtuslab.inkuire.engine.serialization.EngineModelSerializers
import org.virtuslab.inkuire.engine.service.DefaultSignatureResolver
import org.virtuslab.inkuire.engine.service.SubstitutionMatchService
import org.virtuslab.inkuire.engine.utils.Monoid

import java.io.File
import java.net.URL
import scala.io.Source
import scala.util.chaining._

class InkuireTestService(path: String) {

  private def getURLs(url: URL, filesExtension: String): List[URL] = {
    if (url.toURI.getScheme.toLowerCase == "file" && new File(url.toURI).isDirectory)
      new File(url.toURI).listFiles(_.getName.endsWith(filesExtension)).map(_.toURI.toURL).toList
    else List(url)
  }

  private def getURLContent(url: URL) = Source.fromInputStream(url.openStream()).getLines().mkString

  val db: InkuireDb =
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

  val matchService = new SubstitutionMatchService(db)
  val resolver     = new DefaultSignatureResolver(db)
  val parser       = new ScalaSignatureParserService

  def query(q: String): Seq[ExternalSignature] = {
    parser
      .parse(q)
      .flatMap(resolver.resolve)
      .map(matchService.findMatches)
      .toOption
      .toSeq
      .flatMap(_.map(_._1))
  }

}