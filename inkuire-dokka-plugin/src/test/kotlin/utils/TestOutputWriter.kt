package utils

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.renderers.OutputWriter
import org.jetbrains.dokka.plugability.DokkaPlugin

class TestOutputWriterPlugin(failOnOverwrite: Boolean = true): DokkaPlugin() {
    val writer = TestOutputWriter(failOnOverwrite)

    val testWriter by extending { plugin<DokkaBase>().outputWriter with writer }
}

class TestOutputWriter(private val failOnOverwrite: Boolean = true): OutputWriter {
    val contents: Map<String, String> get() = _contents

    private val _contents = mutableMapOf<String, String>()

    override fun write(path: String, text: String, ext: String) {
        val fullPath = "$path$ext"
        _contents.putIfAbsent(fullPath, text)?.also {
            if (failOnOverwrite) throw AssertionError("File $fullPath is being overwritten.")
        }
    }

    override fun writeResources(pathFrom: String, pathTo: String) = write(pathTo, "*** content of $pathFrom ***", "")
}