import org.virtuslab.inkuire.engine.common.model._
import org.virtuslab.inkuire.engine.common.parser.{BaseSignatureParserService, KotlinSignatureParserService}
import org.virtuslab.inkuire.engine.common.service.{AncestryGraph, FluffMatchService, KotlinExternalSignaturePrettifier}
import Type.StringTypeOps
import cats.implicits.catsSyntaxOptionId
import org.scalajs.dom.ext.Ajax
import org.virtuslab.inkuire.engine.common.model.Engine.Env

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scalajs.js.annotation.JSExportTopLevel
import scalajs.concurrent.JSExecutionContext.Implicits.queue
class DbTest {
  private def getURLContent(url: String) = Ajax.get(url).map(_.responseText).fallbackTo(Future("[]"))

  private val functionSources = Seq(
    "./functionskotlin-stdlib_kotlin-stdlib-common.inkuire.fdb",
    "./functionskotlin-stdlib_kotlin-stdlib-java-common.inkuire.fdb",
    "./functionskotlin-stdlib_kotlin-stdlib-jdk7.inkuire.fdb",
    "./functionskotlin-stdlib_kotlin-stdlib-jdk8.inkuire.fdb"
  ).map(getURLContent)
  private val graphsSources = Seq(
    "./ancestryGraphkotlin-stdlib_kotlin-stdlib-jdk8.inkuire.adb",
    "./ancestryGraphkotlin-stdlib_kotlin-stdlib-jdk7.inkuire.adb",
    "./ancestryGraphkotlin-stdlib_kotlin-stdlib-common.inkuire.adb",
    "./ancestryGraphkotlin-stdlib_kotlin-stdlib-java-common.inkuire.adb"
  ).map(getURLContent)
  private val parser = new KotlinSignatureParserService()
  val env = for {
    functions <- Future.sequence(functionSources)
    ancestryGraphs <- Future.sequence(graphsSources)
    db <- Future(InkuireDb.read(functions.toList, ancestryGraphs.toList))
    env = db match {
      case Right(v) =>
        Env(
          v,
          new FluffMatchService(v),
          new KotlinExternalSignaturePrettifier(),
          new KotlinSignatureParserService(),
          null
        )
    }
  } yield env

  def tryMatch(query: String) = {
    val e = env.value.get.get
    e.parser.parse(query) match {
      case Right(sig) => e.prettifier.prettify(e.matcher.|??|(sig))
      case Left(e)    => e
    }
  }

}

object App {
  val db = new DbTest()
  def main(args: Array[String]): Unit = {}
  @JSExportTopLevel("tryMatch")
  def tryMatch(query: String) = println(db.tryMatch(query))
}
