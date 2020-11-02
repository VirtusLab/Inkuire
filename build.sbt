
ThisBuild / organization := "org.virtuslab.inkuire"

name := "inkuire"

ThisBuild / scalaVersion := "2.13.3"

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

ThisBuild / circeDependency := Seq(
  "io.circe" %%% "circe-core" % "0.13.0",
  "io.circe" %%% "circe-parser" % "0.13.0",
  "io.circe" %%% "circe-generic" % "0.13.0"
)

lazy val commonScalaRoot = project
  .in(file("./commonScala"))
  .aggregate(commonScala.js, commonScala.jvm)

lazy val commonScala = crossProject(JSPlatform, JVMPlatform)
  .in(file("./commonScala"))
  .settings(
    name := "inkuire-common-scala",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= circeDependency.value
  )

lazy val engineCommonRoot = project
  .in(file("./engineCommon"))
  .aggregate(engineCommon.js, engineCommon.jvm)

lazy val engineCommon = crossProject(JSPlatform, JVMPlatform)
  .in(file("./engineCommon"))
  .settings(
    name := "inkuire-engine-common",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.softwaremill.quicklens" %%% "quicklens" % "1.6.1",
      "io.scalaland" %%% "chimney" % "0.6.0",
      "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.1.2",
      "com.softwaremill.diffx" %%% "diffx-scalatest" % "0.3.29" % Test
    ) ++ circeDependency.value ++ catsDependency.value
  )
  .dependsOn(commonScala)

lazy val engineHttp = project
  .in(file("./engineHttp"))
  .settings(
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
      "com.lihaoyi" %% "scalatags" % "0.9.1",
    ) ++ catsDependency.value,
    //Test
    libraryDependencies ++= Seq(
      "junit" % "junit" % "4.12" % Test,
      "org.scalatest" %% "scalatest" % "3.2.2" % Test,
      "com.softwaremill.diffx" %% "diffx-scalatest" % "0.3.28" % Test
    ),
    mainClass in assembly := Some("org.virtuslab.inkuire.engine.http.Main")
  )
  .dependsOn(engineCommon.jvm)

lazy val engineJS = project
  .in(file("./engineJs"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
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
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(engineCommon.js)
