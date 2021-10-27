import java.io.FileInputStream
import java.util.Properties

ThisBuild / name := "inkuire"
ThisBuild / organization := "org.virtuslab.inkuire"
ThisBuild / version := "1.0.0-M3"

ThisBuild / bintrayRepository := "Inkuire"
ThisBuild / bintrayOrganization := Some("virtuslab")

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
    "filip.zybala@gmail.com",
    url("https://twitter.com/pikinier20")
  )
)

publish / skip := true
ThisBuild / scalaVersion := "3.0.2"

// Waiting for Scala 3 scalafix support
// ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
// ThisBuild / semanticdbEnabled := true
// ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
// ThisBuild / scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value)
// ThisBuild / scalacOptions ++= Seq(
//   "-Yrangepos",
//   "-Ywarn-unused"
// )

val http4sVersion = "0.22.2"
val catsVersion = "2.6.1"
val catsEffectVersion = "2.5.3"
val circeVersion = "0.14.1"
val monixVersion = "3.4.0"

scalacOptions ++= Seq("-Ypartial-unification")

lazy val engineCommon = crossProject(JSPlatform, JVMPlatform)
  .in(file("engineCommon"))
  .settings(
    name := "inkuire-engine-common",
    libraryDependencies ++= Seq(
      "com.softwaremill.quicklens" %%% "quicklens" % "1.7.5",
      "org.scala-lang.modules" %%% "scala-parser-combinators" % "2.0.0",
      "org.typelevel" %%% "cats-core" % catsVersion,
      "org.typelevel" %%% "cats-effect" % catsEffectVersion,
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "com.lihaoyi" % "pprint_2.13" % "0.6.6"
    ),
  )

lazy val engineHttp = project
  .in(file("./engineHttp"))
  .settings(
    name := "engine-http",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.slf4j" % "slf4j-simple" % "1.7.30",
      "com.lihaoyi" % "scalatags_2.13" % "0.9.4"
    ),
    assembly / mainClass := Some("org.virtuslab.inkuire.engine.http.Main")
  )
  .dependsOn(engineCommon.jvm)

lazy val engineJS = project
  .in(file("./engineJs"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "engine-js",
    libraryDependencies ++= Seq(
      "io.monix" %%% "monix" % monixVersion,
      "io.monix" %%% "monix-reactive" % monixVersion,
      "org.scala-js" % "scalajs-dom_sjs1_2.13" % "1.2.0"
    ),
    scalaJSUseMainModuleInitializer := true,
  )
  .dependsOn(engineCommon.js)
