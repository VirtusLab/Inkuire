rootProject.name = "Inkuire"
include("inkuire-dokka-plugin")
include("inkuire-integration-tests")
include("inkuire-engine")
include("inkuire-common")

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "1.4.0-rc"
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven ("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}