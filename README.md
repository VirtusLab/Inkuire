![CI](https://github.com/VirtusLab/Inkuire/workflows/CI/badge.svg)

---
![image](engine/src/main/resources/assets/logoinkuire.png)

# Inkuire - query engine and dokka plugin generating database for executing semantic queries

---

The project is developed as the engineering thesis at AGH University of Technology in collaboration with VirtusLab - the 
creator of [dokka](https://github.com/Kotlin/dokka).

The goal of the project is to provide way of searching extensive functions and methods by given signature for JVM languages.
Currently supported langauge is Kotlin. Including Scala is taken into account.

---

Feel free to test tool locally or remotely at [inkuire.me](https://inkuire.herokuapp.com) which is hosted instance of engine fed with Kotlin Standard Library.

If you see any bugs, please inform us about it by creating issue in our repository [Github](https://github.com/VirtusLab/Inkuire)

---

### Basics

Inkuire consists of three modules:
1. Inkuire Dokka Plugin - This module is responsible for generating functions and types database from source code.
2. Inkuire Engine - This module is responsible for executing queries.
3. Inkuire IntelliJ Plugin - This module allows you to run queries from IntelliJ.

---
### Input signatures

Currently tool accepts any correct Kotlin signature.

---

### Running Inkuire

#### Inkuire Dokka Plugin

In order to generate database, you need to configure Dokka in your project and apply our plugin. 
Running dokka task will generate .json files containing information further needed by engine.
Make sure you configure proper version of Dokka.

See example: [Kotlin Standard Library](https://github.com/BarkingBad/kotlin-dokka-stdlib/blob/inkuire/build.gradle)

Since our tool is not yet published anywhere, you need to publish it locally by calling `./gradlew publishToMavenLocal`

#### Inkuire Engine

Inkuire Engine is the main module of this project. It provides HTTP service that allows you to run queries using its endpoints.


Inkuire Engine can be run in two ways:
* Using Gradle - `./gradlew run --args'(Place for CLI args)'`
* JAR - You can generate fatJar with `./gradlew fatJar` and then run JAR by `java -jar jar_name.jar (Place for CLI args)`

##### CLI Arguments

* Address - `--address` - Mandatory argument that defines address to which app should bind
* Port - `--port | -p` - Mandatory argument that defines port to which app should bind
* Ancestry graph paths - `{ --ancestry | -a }` - Arguments that define URLs to ancestry graph JSONs
* Function database paths - `{ --database | -d }` - Arguments that define URLs to function database JSONs

Don't forget that URLs need to have protocol prefix, so if you want to provide path to a local file, it needs to be in `file://(path)` format

##### API Endpoints

* `/` - It redirects you to "/query"

* `/query` - Static html page that allows you to run query by filling in form. It also contains some examples.

* `/forSignature` - This endpoint takes signature as param and returns JSON with results. It can be used by external tools to get query results.

##### `/forSignature` usage

Endpoint handles GET requests in format `/forSignature?signature=SIGNATURE` where SIGNATURE is query signature.

Output format is JSON with fields:
* `query` - Contains executed query
* `matches` - Contains array of objects with fields: 
    * `prettifiedSignature` - Matched signature
    * `functionName` - Name of matched function
    * `localization` - Localization of matched function
    
#### Inkuire IntelliJ Plugin

IntelliJ plugin is in early developement, but if you want to try it, you need to run Inkuire Engine locally, and then run `./gradlew runIde`. It will run IntelliJ instance with our plugin enabled. You can open plugin by pressing: `Tools->Inkuire` 

---

#### Importing project in IntelliJ IDEA

There are two build tools included in building project. To obtain best experinece of developing the code while having
code completion and type inference one should import project to IDEA following these steps:

1. Import the project from build.sbt and wait for IntelliJ to index all the files. You should be able to see sbt ont the right panel as well as the submodules it is governing.
2. Go to File -> Project Structure -> Modules and select Add -> Import module
3. Import project from build.gradle.kts and wait for IntelliJ to index all files.
4. You should be able to see both sbt and gradle widgets on right bar.
5. When developing sbt sources or gradle sources make sure to have project reloaded for your build tool project schema. Just open sbt/gradle sidemenu and click `Reload all [sbt/Gradle] projects`

