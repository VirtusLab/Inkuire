import java.io.FileInputStream
import java.util.Properties

ThisBuild / name := "inkuire"
ThisBuild / organization := "org.virtuslab.inkuire"
ThisBuild / version := "0.1.2-SNAPSHOT"

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
    "fzybala@virtuslab.com",
    url("https://twitter.com/pikinier20")
  )
)

publish / skip := true
ThisBuild / scalaVersion := "2.13.4"

val http4sVersion = "0.21.0"
val catsVersion = "2.2.0"
val circeVersion = "0.13.0"

lazy val engineCommon = crossProject(JSPlatform, JVMPlatform)
  .in(file("engineCommon"))
  .settings(
    name := "inkuire-engine-common",
    libraryDependencies ++= Seq(
      "com.softwaremill.quicklens" %%% "quicklens" % "1.7.2",
      "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.1.2",
      "org.typelevel" %%% "cats-core" % catsVersion,
      "org.typelevel" %%% "cats-effect" % catsVersion,
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion
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
      "com.lihaoyi" %% "scalatags" % "0.9.4"
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
  )
  .dependsOn(engineCommon.js)
