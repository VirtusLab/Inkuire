plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
}

group = "org.virtuslab"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

publishing {
    publications {
        register<MavenPublication>("inkuireCommon") {
            artifactId = "inkuire-common"
            from(components["java"])
        }
    }
}
