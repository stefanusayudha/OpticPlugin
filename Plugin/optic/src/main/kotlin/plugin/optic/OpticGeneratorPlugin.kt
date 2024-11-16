package plugin.optic

import org.gradle.api.Plugin
import org.gradle.api.Project
import plugin.optic.util.GENERATE_LENS_TEMPLATE
import plugin.optic.util.LENS_TEMPLATE
import plugin.optic.util.addToSourceSet
import plugin.optic.util.contain
import plugin.optic.util.dumpFile
import java.io.File

class OpticGeneratorPlugin : Plugin<Project> {

    companion object {
        const val BLOCK_PREPOSSESSOR = "// #BEGIN Auto Generated Lenses, Do not Modify!"
        const val BLOCK_SUBPOSSESSOR = "// #END Auto Generated Lenses, Do not Modify!"
        const val SIGNATURE = "// Powered by: Singularity Indonesia"
        const val TARGET_ANNOTATION = "@GenerateLens"
    }

    private val targetDir = "build/generated/kotlin/lenses/"
    private val Project.namespace get() = group.toString().lowercase()

    override fun apply(
        target: Project
    ) {
        // adding targetDir to source set
        addToSourceSet(target, targetDir)
        generateBasicUtils(target.projectDir)

        // dump target files
        val targetFiles = getTargetFiles(target.projectDir)

        // create lenses
        val lenses = generateLenses(targetFiles)

        // generate files
        writeToFile(lenses)
    }

    // generate Lens class and GenerateLens tobe use
    private fun generateBasicUtils(projectDir: File) {
        val path = "${targetDir}io/github/stefanusayudha/optic/"

        // print Lens.kt
        val file1 = File(projectDir, "${path}Lens.kt")
        file1.parentFile.mkdirs()
        file1.writeText(LENS_TEMPLATE)

        // print GenerateLens
        val file2 = File(projectDir, "${path}GenerateAnnotation.kt")
        file2.parentFile.mkdirs()
        file2.writeText(GENERATE_LENS_TEMPLATE)
    }

    private fun getTargetFiles(projectDir: File): Sequence<File> {
        return projectDir.dumpFile(".kt").filter { file -> file.contain(TARGET_ANNOTATION) }
    }

    private fun generateLenses(files: Sequence<File>): Sequence<LensBuilder> {
        return files.map { file -> LensBuilder(file) }
    }

    private fun writeToFile(lenses: Sequence<LensBuilder>) {
        lenses
            .onEach { lens ->
                printToFile(
                    file = lens.source,
                    content = lens.content
                )
            }
            .toList()
    }

    private fun printToFile(file: File, content: String) {
        val fileContent = file.readLines()
        val beginIndex = fileContent.indexOfFirst { s -> s.contains(BLOCK_PREPOSSESSOR) }
            .takeIf { it >= 0 } ?: fileContent.size
        val endIndex = fileContent.indexOfFirst { s -> s.contains(BLOCK_SUBPOSSESSOR) }
            .takeIf { it >= 0 } ?: fileContent.size

        val topContent = fileContent.subList(0, beginIndex)
        val endContent = fileContent.subList(
            (endIndex + 1).takeIf { it <= fileContent.size } ?: fileContent.size,
            fileContent.size
        )
        val newContent = (topContent + content + endContent).foldIndexed("") { index, acc, n ->
            if (index != 0) "$acc\n$n" else "$acc$n"
        }

        file.writeText(newContent)
    }
}
