group = "org.virtuslab"
version = "1.0-SNAPSHOT"

plugins{
    scala
    id("com.github.maiflai.scalatest") version "0.26"
}

repositories{
    jcenter()
    mavenCentral()
}

dependencies{
    val scalaVersion = "2.13"

    implementation("org.scala-lang:scala-library:$scalaVersion.1")

    testImplementation("junit:junit:4.13")
    testImplementation("org.scalatest:scalatest_$scalaVersion:3.1.1")
    testRuntimeOnly("org.scala-lang.modules:scala-xml_$scalaVersion:1.3.0")
    implementation("org.scala-lang.modules:scala-parser-combinators_$scalaVersion:1.1.2")
    testRuntimeOnly("com.vladsch.flexmark:flexmark-all:0.35.10")

    implementation("org.typelevel:cats-core_$scalaVersion:2.1.1")
    implementation("org.typelevel:cats-effect_$scalaVersion:2.1.1")
    implementation("org.typelevel:cats-mtl-core_$scalaVersion:0.7.1")

    implementation("com.softwaremill.quicklens:quicklens_$scalaVersion:1.5.0")
    implementation("com.softwaremill.diffx:diffx-core_$scalaVersion:0.3.28")
    implementation("com.softwaremill.diffx:diffx-scalatest_$scalaVersion:0.3.28")

    implementation("io.scalaland:chimney_$scalaVersion:0.5.1")
}

task("runCli", JavaExec::class) {
    main = "org.virtuslab.inkuire.engine.cli.Main"
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}
