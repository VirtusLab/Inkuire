plugins {
    kotlin("jvm") apply false
    id("com.jfrog.bintray")
}

val globalProperties = java.util.Properties()
file("global.properties").inputStream().run {
    globalProperties.load(this)
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("com.jfrog.bintray")
    }

    group = "org.virtuslab.inkuire"
    version = globalProperties.getProperty("version")

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

    bintray {
        user = System.getenv("BINTRAY_USER")
        key = System.getenv("BINTRAY_PASS")
        setPublications("MavenJava")
        publish = true
        pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
            repo = "Inkuire"
            userOrg = "virtuslab"
            name = project.name
            setLicenses("Apache-2.0")
            vcsUrl = "https://github.com/VirtusLab/Inkuire.git"
        })
    }
}


