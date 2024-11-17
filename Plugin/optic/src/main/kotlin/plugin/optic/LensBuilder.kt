package plugin.optic

import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import plugin.optic.OpticGeneratorPlugin.Companion.BLOCK_PREPOSSESSOR
import plugin.optic.OpticGeneratorPlugin.Companion.BLOCK_SUBPOSSESSOR
import java.io.File

class LensBuilder(
    val source: File
) {
    companion object {
        const val DATA_CLASS_IDENTIFIER = "data class"
    }

    val fileContent: List<String> get() = source.useLines { lines -> lines.toList() }

    // proto content, the generated lenses block
    val proto: String
        get() {
            val annotatedProperties = getAnnotatedProperties(fileContent)
            val groupedAnnotatedProperties = annotatedProperties
                .groupBy { property -> property.className }
                .map { entry -> toProtoClass(entry) }
            val contentBody =
                groupedAnnotatedProperties.foldIndexed("") { index, acc, e -> if (index != 0) "$acc\n\n$e" else "$acc\n$e" }

            return listOf(BLOCK_PREPOSSESSOR, OpticGeneratorPlugin.SIGNATURE)
                .plus(contentBody)
                .plus(BLOCK_SUBPOSSESSOR)
                .foldIndexed("") { index, acc, n ->
                    if (index != 0) "$acc\n$n" else "$acc$n"
                }
        }

    private fun getAnnotatedProperties(content: List<String>): List<AnnotatedProperty> {

        val annotationPositions =
            content.foldIndexed(listOf<Int>()) { index, acc, e ->
                if (e.contains(OpticGeneratorPlugin.TARGET_ANNOTATION)) acc + index else acc
            }

        return annotationPositions.map { i ->

            val property = content[i + 1]
            val propertyName = property.split(":").first().replace("val", " ").replace("var", " ").replace(" ", "")
            val propertyType = property.split(":").last().split("=").first().replace(",", " ").replace(" ", "")

            var className = run {
                var searchedIndex = i

                while (searchedIndex >= 0) {
                    searchedIndex--
                    val suspect = content[searchedIndex]
                    if (suspect.contains(DATA_CLASS_IDENTIFIER)) {
                        return@run suspect.split("class").last().split("(").first()
                        break
                    }
                }

                throw Error("Class name not found for $propertyName")
            }

            AnnotatedProperty(
                className = className,
                propertyName = propertyName,
                propertyType = propertyType
            )
        }
    }

    private fun toProtoClass(entry: Map.Entry<String, List<AnnotatedProperty>>): String {

        val className = "${entry.key}Lens"
        val propertyLenses = entry.value
            .map { property ->
                val propName = "${property.propertyName.uppercaseFirstChar()}Lens"

                listOf(
                    "val $propName = object : Lens<${property.className}, ${property.propertyType}> {",
                    "    override fun modify(",
                    "        parent: ${property.className},",
                    "        children: (${property.propertyType}) -> ${property.propertyType}",
                    "    ): ${property.className} = parent.copy(${property.propertyName} = children.invoke(parent.${property.propertyName}))",
                    "",
                    "    override fun get(parent: ${property.className}): ${property.propertyType} = parent.${property.propertyName}",
                    "}"
                )
            }
            .flatten()

        val protoClass = listOf("object ${className.uppercaseFirstChar()} {")
            .plus(
                // add indent
                propertyLenses.map { string -> "    $string" }
            )
            .plus("}")
            .foldIndexed("") { index, acc, e ->
                if (index != 0) {
                    "$acc\n$e"
                } else {
                    "$acc$e"
                }
            }

        return protoClass
    }

    fun printToFile() {
        // new file content where the lenses block is inserted
        val newFileContent: String = run {
            val beginIndex = fileContent.indexOfFirst { s -> s.contains(BLOCK_PREPOSSESSOR) }
                .takeIf { it >= 0 } ?: fileContent.size
            val endIndex = fileContent.indexOfFirst { s -> s.contains(BLOCK_SUBPOSSESSOR) }
                .takeIf { it >= 0 } ?: fileContent.size

            val topContent = fileContent.subList(0, beginIndex)
            val endContent = fileContent.subList(
                (endIndex + 1).takeIf { it <= fileContent.size } ?: fileContent.size,
                fileContent.size
            )
            val newContent = (topContent + proto + endContent).foldIndexed("") { index, acc, n ->
                if (index != 0) "$acc\n$n" else "$acc$n"
            }

            newContent
        }

        source.writeText(newFileContent)
    }
}