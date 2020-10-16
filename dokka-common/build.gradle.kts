plugins {
    `maven-publish`
}

group = "org.virtuslab"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/dokka/dev")
    jcenter()
    mavenLocal()
}

dependencies {

    val dokkaVersion: String by project

    api(kotlin("stdlib-jdk8"))
    compileOnly("org.jetbrains.dokka:dokka-core:$dokkaVersion")
    compileOnly("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    api("com.google.code.gson:gson:2.8.6")
    testApi("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    testApi("org.jetbrains.dokka:dokka-test-api:$dokkaVersion")
    api("junit:junit:4.13")
    api(project(":common"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
}

publishing {
    publications {
        register<MavenPublication>("dokkaCommon") {
            artifactId = "inkuire-dokka-common"
            from(components["java"])
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
