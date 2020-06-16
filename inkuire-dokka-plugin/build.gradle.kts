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
    maven { url = uri("https://repo.spring.io/libs-release") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compileOnly("org.jetbrains.dokka:dokka-core:0.11.0-dev-41")
    compileOnly("org.jetbrains.dokka:dokka-base:0.11.0-dev-41")
    implementation("com.google.code.gson:gson:2.8.6")
    testImplementation("org.jetbrains.dokka:dokka-base:0.11.0-dev-40")
    testImplementation("org.jetbrains.dokka:dokka-test-api:0.11.0-dev-40")
    implementation("junit:junit:4.13")
    implementation(project(":inkuire-common"))
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