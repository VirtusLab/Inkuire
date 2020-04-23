plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
}

group = "org.virtuslab"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("http://dl.bintray.com/kotlin/kotlin-dev")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compileOnly("org.jetbrains.dokka:dokka-core:0.11.0-dev-40")
    compileOnly("org.jetbrains.dokka:dokka-base:0.11.0-dev-40")
    implementation("com.google.code.gson:gson:2.8.6")
}

publishing {
    publications {
        register<MavenPublication>("inkuirePlugin") {
            artifactId = "inkuire-plugin"
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