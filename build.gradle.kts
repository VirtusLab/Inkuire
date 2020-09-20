group = "org.virtuslab"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") apply false
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }
}
