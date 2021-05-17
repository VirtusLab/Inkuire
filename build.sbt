import java.io.FileInputStream
import java.util.Properties

ThisBuild / organization := "org.virtuslab.inkuire"
ThisBuild / homepage := Some(url("https://github.com/VirtusLab/Inkuire"))
ThisBuild / licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / developers := List(
  Developer(
    "KacperFKorban",
    "Kacper Korban",
    "kacper.f.korban@gmail.com",
    url("https://twitter.com/KacperKorban")
  ),
  Developer(
    "BarkingBad",
    "Andrzej Ratajczak",
    "andrzej.ratajczak98@gmail.com",
    url("https://twitter.com/aj_ratajczak")
  ),
  Developer(
    "pikinier20",
    "Filip Zybała",
    "fzybala@virtuslab.com",
    url("https://twitter.com/pikinier20")
  )
)

name := "Inkuire"
skip in publish := true
ThisBuild / scalaVersion := "2.13.4"

val fileInputStream = new FileInputStream(new File("global.properties"))
val globalProperties = new Properties()
val _ = globalProperties.load(fileInputStream) // :')
val inkuireVersion = globalProperties.getProperty("version")

val http4sVersion = "0.21.0"

val catsDependency = settingKey[Seq[ModuleID]]("Dependency to cats library")

val circeDependency = settingKey[Seq[ModuleID]]("Dependency to circe library")
// TODO: I would like to use it, but it appends wrong libs to JS projects :/
ThisBuild / catsDependency := Seq(
  "org.typelevel" %%% "cats-core" % "2.2.0",
  "org.typelevel" %%% "cats-effect" % "2.2.0",
  "org.typelevel" %%% "cats-mtl-core" % "0.7.1"
)

val stage = taskKey[Unit]("Stage task")

val Stage = config("stage")

stage := {
  assembly.in(engineHttp).value
}

(engineJS / Compile / fastOptJS) := (engineJS / Compile / fastOptJS).dependsOn(engineJS / Compile / compile).value
(engineJS / Compile / fullOptJS) := (engineJS / Compile / fullOptJS).dependsOn(engineJS / Compile / compile).value

ThisBuild / circeDependency := Seq(
  "io.circe" %%% "circe-core" % "0.13.0",
  "io.circe" %%% "circe-parser" % "0.13.0",
  "io.circe" %%% "circe-generic" % "0.13.0"
)

lazy val commonScalaRoot = project
  .in(file("./commonScala"))
  .aggregate(commonScala.js, commonScala.jvm)
  .settings(
    name := "common-scala-root",
    version := inkuireVersion,
    bintrayRepository := "Inkuire",
    bintrayOrganization := Some("virtuslab"),
    licenses ++= Seq(("Apache-2.0", url("https://opensource.org/licenses/Apache-2.0")))
  )

lazy val commonScala = crossProject(JSPlatform, JVMPlatform)
  .in(file("./commonScala"))
  .settings(
    name := "common-scala",
    version := inkuireVersion,
    libraryDependencies ++= circeDependency.value,
    bintrayRepository := "Inkuire",
    bintrayOrganization := Some("virtuslab"),
    licenses ++= Seq(("Apache-2.0", url("https://opensource.org/licenses/Apache-2.0")))
  )

lazy val engineCommonRoot = project
  .in(file("./engineCommon"))
  .aggregate(engineCommon.js, engineCommon.jvm)
  .settings(
    name := "engine-common-root",
    version := inkuireVersion,
    bintrayRepository := "Inkuire",
    bintrayOrganization := Some("virtuslab"),
    licenses ++= Seq(("Apache-2.0", url("https://opensource.org/licenses/Apache-2.0")))
  )

lazy val engineCommon = crossProject(JSPlatform, JVMPlatform)
  .in(file("./engineCommon"))
  .settings(
    name := "engine-common",
    version := inkuireVersion,
    libraryDependencies ++= Seq(
      "com.softwaremill.quicklens" %%% "quicklens" % "1.6.1",
      "io.scalaland" %%% "chimney" % "0.6.0",
      "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.1.2",
      "com.softwaremill.diffx" %%% "diffx-scalatest" % "0.3.29" % Test
    ) ++ circeDependency.value ++ catsDependency.value,
    bintrayRepository := "Inkuire",
    bintrayOrganization := Some("virtuslab"),
    licenses ++= Seq(("Apache-2.0", url("https://opensource.org/licenses/Apache-2.0")))
  )
  .dependsOn(commonScala)

lazy val engineHttp = project
  .in(file("./engineHttp"))
  .settings(
    name := "engine-http",
    libraryDependencies ++= Seq(
      "com.softwaremill.quicklens" %% "quicklens" % "1.5.0",
      "com.softwaremill.diffx" %% "diffx-core" % "0.3.28",
      "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
      "com.vladsch.flexmark" % "flexmark-all" % "0.35.10",
      "io.scalaland" %% "chimney" % "0.5.1",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
      "com.github.pureconfig" %% "pureconfig" % "0.14.0",
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-server" % http4sVersion,
      "org.http4s" %% "http4s-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.slf4j" % "slf4j-simple" % "1.7.30",
      "com.lihaoyi" %% "scalatags" % "0.9.1"
    ) ++ catsDependency.value,
    //Test
    libraryDependencies ++= Seq(
      "junit" % "junit" % "4.12" % Test,
      "org.scalatest" %% "scalatest" % "3.2.2" % Test,
      "com.softwaremill.diffx" %% "diffx-scalatest" % "0.3.28" % Test
    ),
    version := inkuireVersion,
    bintrayRepository := "Inkuire",
    bintrayOrganization := Some("virtuslab"),
    licenses ++= Seq(("Apache-2.0", url("https://opensource.org/licenses/Apache-2.0"))),
    mainClass in assembly := Some("org.virtuslab.inkuire.engine.http.Main")
  )
  .dependsOn(engineCommon.jvm)

lazy val engineJS = project
  .in(file("./engineJs"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "engine-js",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client" %%% "core" % "2.2.9",
      "org.typelevel" %%% "cats-core" % "2.2.0",
      "org.typelevel" %%% "cats-effect" % "2.2.0",
      "org.typelevel" %%% "cats-mtl-core" % "0.7.1",
      "io.circe" %%% "circe-core" % "0.13.0",
      "io.circe" %%% "circe-parser" % "0.13.0",
      "io.circe" %%% "circe-generic" % "0.13.0",
      "io.monix" %%% "monix" % "3.2.2",
      "io.monix" %%% "monix-reactive" % "3.2.2"
    ),
    scalaJSUseMainModuleInitializer := true,
    version := inkuireVersion,
    bintrayRepository := "Inkuire",
    bintrayOrganization := Some("virtuslab"),
    licenses ++= Seq(("Apache-2.0", url("https://opensource.org/licenses/Apache-2.0")))
  )
  .dependsOn(engineCommon.js)
