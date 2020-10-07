group = "org.virtuslab"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") apply false
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    repositories {
        jcenter()
    }

    val ktlint by configurations.creating

    dependencies {
        ktlint("com.pinterest:ktlint:0.39.0")
    }

    val ktlintCheck by tasks.creating(JavaExec::class) {
        description = "Check Kotlin code style."
        main = "com.pinterest.ktlint.Main"
        classpath = ktlint
        args = listOf("src/**/*.kt")
    }

    val ktlintFormat by tasks.creating(JavaExec::class) {
        description = "Fix Kotlin code style deviations."
        main = "com.pinterest.ktlint.Main"
        classpath = ktlint
        args = listOf("-F", "src/**/*.kt")
    }
}
