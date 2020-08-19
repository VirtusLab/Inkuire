rootProject.name = "Inkuire"
include("inkuire-dokka-plugin")
include("inkuire-integration-tests")
include("inkuire-engine")
include("inkuire-common")
include("inkuire-intellij-plugin")

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "1.4.0"
    }
}
