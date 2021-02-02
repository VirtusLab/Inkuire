plugins {
    `maven-publish`
}

group = "org.virtuslab"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/dokka/dev")
}

dependencies {
    val dokkaVersion: String by project

    compileOnly("org.jetbrains.dokka:dokka-core:$dokkaVersion")
    implementation("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    implementation(project(":dokka-common"))

    implementation(kotlin("stdlib-jdk8"))
    implementation("com.google.code.gson:gson:2.8.6")
    testImplementation("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    testImplementation("org.jetbrains.dokka:dokka-test-api:$dokkaVersion")
    implementation("junit:junit:4.13")
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
}

publishing {
    publications {
        register<MavenPublication>("htmlExtension") {
            artifactId = "inkuire-html-extension"
            from(components["java"])
        }
    }
}

val copy by tasks.creating(Copy::class) {
    from("$rootDir/engineJs/target/scala-2.13/", "$rootDir/engineJs/src/main/resources")
    into("$rootDir/dokka-common/dokka-html-inkuire-extension/src/main/resources/inkuire")
    include("enginejs-opt.js", "inkuire-styles.css", "inkuire-search.png", "inkuire-worker.js")
    rename("inkuire-worker.js", "scripts/inkuire-worker.js")
    rename("enginejs-opt.js", "scripts/inkuire.js")
    rename("inkuire-styles.css", "styles/inkuire-styles.css")
    rename("inkuire-search.png", "images/inkuire-search.png")
}


tasks.withType<AbstractPublishToMaven>().configureEach {
    this.dependsOn(copy)
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
