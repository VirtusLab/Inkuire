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

    compileOnly("org.jetbrains.dokka:dokka-core:$dokkaVersion")
    compileOnly("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    implementation(project(":dokka-common"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
