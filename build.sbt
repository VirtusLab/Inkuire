
val scala213 = "2.13.8"
val scala3 = "3.1.3"

val orgSettings = Seq(
  organization := "org.virtuslab",
  homepage := Some(url("https://github.com/VirtusLab/Inkuire")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "KacperFKorban",
      "Kacper Korban",
      "kacper.f.korban@gmail.com",
      url("https://twitter.com/KacperFKorban")
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
  ),
)

// This is needed so that simple `sbt test` doesn't crash with OOM error
// (by default sbt tries to execute everyting it can at once)
Global / concurrentRestrictions += Tags.limit(Tags.All, 1)

val commonSettings = orgSettings ++ Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature"
  ),
)

val http4sVersion = "0.23.10"
val circeVersion = "0.14.2"
val monixVersion = "3.4.0"

lazy val root =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(
      name := "inkuire-root",
      publishArtifact := false,
      publish / skip := true
    )
    .aggregate(
      (inkuireEngine.projectRefs ++ inkuireHttp.projectRefs ++ inkuireJs.projectRefs): _*
    )

lazy val inkuireEngine = projectMatrix
  .in(file("engine"))
  .settings(commonSettings)
  .settings(
    name := "inkuire-engine",
    libraryDependencies ++= Seq(
      "com.softwaremill.quicklens" %%% "quicklens" % "1.8.5",
      "org.scala-lang.modules" %%% "scala-parser-combinators" % "2.1.1",
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "org.scalameta" %% "munit" % "0.7.29" % Test
    ),
  )
  .jvmPlatform(
    scalaVersions = Seq(scala3, scala213)
  )
  .jsPlatform(
    scalaVersions = Seq(scala3, scala213)
  )

lazy val inkuireHttp = projectMatrix
  .in(file("http"))
  .settings(commonSettings)
  .settings(
    name := "inkuire-http",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.slf4j" % "slf4j-simple" % "1.7.30",
      "com.lihaoyi" %% "scalatags" % "0.11.1"
    ),
    assembly / mainClass := Some("org.virtuslab.inkuire.http.Main")
  )
  .dependsOn(inkuireEngine)
  .jvmPlatform(
    scalaVersions = Seq(scala3, scala213)
  )

lazy val inkuireJs = projectMatrix
  .in(file("js"))
  .settings(commonSettings)
  .settings(
    name := "inkuire-js",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client" %%% "core" % "2.2.9" cross CrossVersion.for3Use2_13,
      "io.monix" %%% "monix" % monixVersion,
      "io.monix" %%% "monix-reactive" % monixVersion
    ),
    scalaJSUseMainModuleInitializer := true,
  )
  .dependsOn(inkuireEngine)
  .jsPlatform(
    scalaVersions = Seq(scala3, scala213)
  )
