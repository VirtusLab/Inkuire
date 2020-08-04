group = "org.virtuslab"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm")
    scala
}

repositories {
    maven("https://dl.bintray.com/kotlin/kotlin-dev")
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    mavenCentral()
    jcenter()
}

dependencies {
    testImplementation(project(":inkuire-dokka-plugin"))
    testImplementation(project(":inkuire-engine"))

    val dokkaVersion: String by project

    testImplementation("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    testImplementation("org.jetbrains.dokka:dokka-test-api:$dokkaVersion")

    val scalaVersion: String by project

    implementation("org.scala-lang:scala-library:$scalaVersion.1")

    testImplementation("junit:junit:4.13")
    testImplementation("org.scalatest:scalatest_$scalaVersion:3.1.1")
    testImplementation("com.softwaremill.diffx:diffx-scalatest_$scalaVersion:0.3.28")
    testRuntimeOnly("org.scala-lang.modules:scala-xml_$scalaVersion:1.3.0")
    testRuntimeOnly("com.vladsch.flexmark:flexmark-all:0.35.10")
}