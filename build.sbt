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

val http4sVersion = "0.21.23"
val circeVersion = "0.13.0"

lazy val engineCommon = crossProject(JVMPlatform)
  .in(file("engineCommon"))
  .settings(
    name := "inkuire-engine-common",
    libraryDependencies ++= Seq(
      "com.softwaremill.quicklens" %%% "quicklens" % "1.7.2",
      "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.1.2",
      "org.typelevel" %%% "cats-core" % "2.6.1",
      "org.typelevel" %%% "cats-effect" % "2.5.1",
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion
    ),
  )

lazy val engineHttp = project
  .in(file("engineHttp"))
  .settings(
    name := "inkuire-engine-http",
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

lazy val tastyGenerator = project
  .in(file("tastyGenerator"))
  .settings(
    name := "inkuire-tasty-generator",
    scalaVersion := "3.0.0",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala3-tasty-inspector_3" % "3.0.0"
    )
  )
  .dependsOn(engineCommon.jvm)