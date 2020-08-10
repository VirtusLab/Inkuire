group = "org.virtuslab"
version = "1.0-SNAPSHOT"

plugins {
    scala
    id("com.github.maiflai.scalatest") version "0.26"

}

repositories {
    jcenter()
    mavenCentral()
    maven {
        url = uri("http://dl.bintray.com/kotlin/kotlin-dev")
    }
}

dependencies {

    val scalaVersion: String by project
    val scalaLibraryVersion: String by project

    implementation("org.scala-lang:scala-library:$scalaLibraryVersion")

    implementation("org.typelevel:cats-core_$scalaVersion:2.1.1")
    implementation("org.typelevel:cats-effect_$scalaVersion:2.1.1")
    implementation("org.typelevel:cats-mtl-core_$scalaVersion:0.7.1")

    implementation("com.softwaremill.quicklens:quicklens_$scalaVersion:1.5.0")
    implementation("com.softwaremill.diffx:diffx-core_$scalaVersion:0.3.28")

    testImplementation("junit:junit:4.13")
    testImplementation("org.scalatest:scalatest_$scalaVersion:3.1.1")
    testImplementation("com.softwaremill.diffx:diffx-scalatest_$scalaVersion:0.3.28")
    testRuntimeOnly("org.scala-lang.modules:scala-xml_$scalaVersion:1.3.0")
    testRuntimeOnly("com.vladsch.flexmark:flexmark-all:0.35.10")

    implementation("io.scalaland:chimney_$scalaVersion:0.5.1")
    implementation("org.scala-lang.modules:scala-parser-combinators_$scalaVersion:1.1.2")
    implementation("com.google.code.gson:gson:2.8.6")

    implementation(project(":inkuire-common"))
}

task("runCli", JavaExec::class) {
    main = "org.virtuslab.inkuire.engine.Main"
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}
