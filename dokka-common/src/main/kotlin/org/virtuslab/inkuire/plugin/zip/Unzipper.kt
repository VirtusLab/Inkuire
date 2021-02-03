package org.virtuslab.inkuire.plugin.zip

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import java.util.Map.entry




object Unzipper {

    fun unzipFiles(jars: List<File>): List<File> =
        jars.map { unzipFileToTempDirectory(it) }

    private fun unzipFileToTempDirectory(file: File): File {
        val destDir = Files.createTempDirectory(file.name + "_sources");
        val fileInputStream = FileInputStream(file)
        val input = ZipInputStream(fileInputStream)
        var ze = input.nextEntry
        while (ze != null) {
            val fileName = ze.name
            val newFilePath = Paths.get(destDir.toAbsolutePath().toString(), fileName)
            if (ze.isDirectory) {
                Files.createDirectories(newFilePath)
            } else {
                val fos = FileOutputStream(newFilePath.toFile())
                input.transferTo(fos)
                fos.close()
            }
            input.closeEntry();
            ze = input.nextEntry;
        }
        input.closeEntry()
        input.close()
        fileInputStream.close()
        return destDir.toFile()
    }
}

