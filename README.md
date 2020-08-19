![CI](https://github.com/VirtusLab/Inkuire/workflows/CI/badge.svg)

---

# Inkuire - query engine and dokka plugin generating database for executing semantic queries

---

The project is developed as the engineering thesis at AGH University of Technology in collaboration with VirtusLab - the 
creator of [dokka](https://github.com/Kotlin/dokka).

The goal of the project is to provide way of searching extensive functions and methods by given signature for JVM languages.
Currently supported langauges are Java and Kotlin. Including Scala is taken into account.

---

### Using inkuire

Due to postponed release of dokka, some artifacts has to be provided manually, e. g.
local maven repository. The Easiest way to achieve that is to publish all artifacts locally from dokka clone.

Inkuire provides various ways to interact with its API. For now, there is REST service and web client or the IntelliJ plugin.

To run web client, clone repository and run gradle task `./gradlew run`

You'll be able to open your browser at address `localhost:8080/query`

The input is any correct signature for Kotlin function. The output is collection of functions, which suits the given signature
either by full match or polymorphic substitution of receiver, arguments or return type.

