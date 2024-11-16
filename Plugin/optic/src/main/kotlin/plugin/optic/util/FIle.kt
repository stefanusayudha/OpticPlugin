package plugin.optic.util

import java.io.BufferedReader
import java.io.File
import java.io.FileReader

fun File.dumpFile(fileNameClue: String): Sequence<File> {
    return listFiles()
        ?.asSequence()
        ?.mapNotNull { file ->
            when {
                !file.isDirectory && file.name.contains(fileNameClue) -> sequenceOf(file)
                file.isDirectory -> file.dumpFile(fileNameClue)
                else -> null
            }
        }
        ?.flatten()
        ?: sequenceOf()
}

fun File.contain(clue: String): Boolean {
    try {
        val reader = BufferedReader(FileReader(this))

        reader.useLines { lines ->
            return lines.any { it.contains(clue) }
        }
    } catch (e: Throwable) {
        return false
    }
}
