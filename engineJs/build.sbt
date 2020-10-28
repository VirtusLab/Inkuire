enablePlugins(ScalaJSPlugin)

name := "inkuire-engine-js"

version := "0.1"

scalaVersion := "2.13.3"

scalaJSUseMainModuleInitializer := true

resolvers ++= Seq(
  Resolver.mavenCentral,
  Resolver.jcenterRepo,
)

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.client" %%% "core" % "2.2.9"
)
