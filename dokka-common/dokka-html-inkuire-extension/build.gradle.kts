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
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.google.code.gson:gson:2.8.6")
    testImplementation("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    testImplementation("org.jetbrains.dokka:dokka-test-api:$dokkaVersion")
    implementation("junit:junit:4.13")
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
}
