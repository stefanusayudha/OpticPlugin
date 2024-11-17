package plugin.optic

import java.io.File

class Cache(
    val projectDir: File
) {
    fun compareToCache(file: File): Boolean {
        val cacheFile = File(projectDir, "${OpticGeneratorPlugin.CACHE_DIR}${file.path}")

        if (!cacheFile.exists())
            return false

        // if cache is older, then it is up to date
        if (cacheFile.lastModified() > file.lastModified())
            return true

        // hopefully we don't need to reach this line
        return compareFile(cacheFile, file)
    }

    fun writeToCache(lensBuilder: Sequence<LensBuilder>) {
        lensBuilder.forEach { builder ->
            val cacheFile = File(projectDir, "${OpticGeneratorPlugin.CACHE_DIR}${builder.source.path}")
            cacheFile.parentFile.mkdirs()
            cacheFile.writeText(builder.newFileContent)
            println("Writing done for ${cacheFile.path}")
        }
    }

    private fun compareFile(file1: File, file2: File): Boolean {
        // Check if file sizes are different
        if (file1.length() != file2.length()) {
            return false
        }

        file1.inputStream().use { input1 ->
            file2.inputStream().use { input2 ->
                val buffer1 = ByteArray(1024)
                val buffer2 = ByteArray(1024)

                while (true) {
                    val bytesRead1 = input1.read(buffer1)
                    val bytesRead2 = input2.read(buffer2)

                    if (bytesRead1 != bytesRead2) {
                        return false // Different file sizes during streaming
                    }
                    if (bytesRead1 == -1) {
                        break // End of file reached
                    }
                    if (!buffer1.contentEquals(buffer2)) {
                        return false // Buffers differ
                    }
                }
            }
        }

        return true
    }
}