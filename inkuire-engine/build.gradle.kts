group = "org.virtuslab"
version = "1.0-SNAPSHOT"

plugins{
    scala
}

repositories{
    jcenter()
}

dependencies{
    val scalaVersion = "2.13"

    implementation("org.scala-lang:scala-library:$scalaVersion.1")

    testImplementation("junit:junit:4.13")
    testImplementation("org.scalatest:scalatest_$scalaVersion:3.1.1")
    testRuntimeOnly("org.scala-lang.modules:scala-xml_$scalaVersion:1.3.0")

    implementation("org.scala-lang.modules:scala-parser-combinators_$scalaVersion:1.1.2")

    implementation("org.typelevel:cats-core_$scalaVersion:2.1.1")
    implementation("org.typelevel:cats-effect_$scalaVersion:2.1.1")
    implementation("org.typelevel:cats-mtl-core_$scalaVersion:0.7.1")
    implementation("org.typelevel:mouse_$scalaVersion:0.25")
}
