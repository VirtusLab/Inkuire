rootProject.name = "Inkuire"
include("common")
include("dokka-common")
include("dokka-common:db-generator")
findProject(":dokka-common:db-generator")?.name = "db-generator"
include("dokka-common:dokka-html-inkuire-extension")
findProject(":dokka-common:dokka-html-inkuire-extension")?.name = "dokka-html-inkuire-extension"
include("integration-tests")
include("intellij-plugin")

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "1.4.10"
        id("com.jfrog.bintray") version "1.8.5"
    }
}
