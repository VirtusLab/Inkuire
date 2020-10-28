name := "engine-http"

resolvers ++= Seq(
  Resolver.mavenCentral,
  Resolver.jcenterRepo
)

scalaVersion := "2.13.3"

val http4sVersion = "0.21.0"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.1.1",
  "org.typelevel" %% "cats-effect" % "2.1.1",
  "org.typelevel" %% "cats-mtl-core" % "0.7.1",
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
)

//Test
libraryDependencies ++= Seq(
  "junit" % "junit" % "4.12" % Test,
  "org.scalatest" %% "scalatest" % "3.2.2" % Test,
  "com.softwaremill.diffx" %% "diffx-scalatest" % "0.3.28" % Test
)

mainClass in assembly := Some("org.virtuslab.inkuire.engine.http.Main")
