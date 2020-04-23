group = "org.virtuslab"
version = "1.0-SNAPSHOT"

plugins{
    scala
}

repositories{
    jcenter()
}

dependencies{
    val scalaVersion = "2.12"

    implementation("org.scala-lang:scala-library:$scalaVersion.8")
}

