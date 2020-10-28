organization := "org.virtuslab.inkuire"

name := "inkuire"

ThisBuild / scalaVersion := "2.13.3"

lazy val commonScalaRoot = project
  .in(file("./commonScala"))
  .aggregate(commonScala.js, commonScala.jvm)

lazy val commonScala = crossProject(JSPlatform, JVMPlatform)
  .in(file("./commonScala"))
  .settings(
    organization := "org.virtuslab.inkuire",
    name := "inkuire-common-scala",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % "0.13.0",
      "io.circe" %%% "circe-parser" % "0.13.0",
      "io.circe" %%% "circe-generic" % "0.13.0"
    )
  )

lazy val engineCommonRoot = project
  .in(file("./engineCommon"))
  .aggregate(engineCommon.js, engineCommon.jvm)

lazy val engineCommon = crossProject(JSPlatform, JVMPlatform)
  .in(file("./engineCommon"))
  .settings(
    organization := "org.virtuslab.inkuire",
    name := "inkuire-engine-common",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.2.0",
      "org.typelevel" %%% "cats-effect" % "2.2.0",
      "org.typelevel" %%% "cats-mtl-core" % "0.7.1",
      "com.softwaremill.quicklens" %%% "quicklens" % "1.6.1",
      "io.scalaland" %%% "chimney" % "0.6.0",
      "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.1.2",
      "io.circe" %%% "circe-core" % "0.13.0",
      "io.circe" %%% "circe-parser" % "0.13.0",
      "io.circe" %%% "circe-generic" % "0.13.0",
      "com.softwaremill.diffx" %%% "diffx-scalatest" % "0.3.29" % Test
    )
  )
  .dependsOn(commonScala)

lazy val engineHttp = project
  .in(file("./engineHttp"))
  .dependsOn(engineCommon.jvm)

lazy val engineJS = project
  .in(file("./engineJs"))
  .dependsOn(commonScala.js, engineCommon.js)
