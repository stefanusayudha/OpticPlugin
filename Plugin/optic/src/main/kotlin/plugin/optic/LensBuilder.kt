package plugin.optic

import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import plugin.optic.OpticGeneratorPlugin.Companion.BLOCK_PREPOSSESSOR
import plugin.optic.OpticGeneratorPlugin.Companion.BLOCK_SUBPOSSESSOR
import java.io.File
import kotlin.collections.foldIndexed
import kotlin.sequences.joinToString
import kotlin.text.contains
import kotlin.text.split

class LensBuilder(
    val source: File
) {
    companion object {
        const val DATA_CLASS_IDENTIFIER = "data class"
    }

    val fileContent: Sequence<String> by lazy {
        source.useLines { lines -> lines.toList().asSequence() }
    }

    // property with annotation @GenerateLens
    val annotatedProperties: Sequence<AnnotatedProperty> by lazy {
        val annotationPositions =
            fileContent.foldIndexed(sequenceOf<Int>()) { index, acc, e ->
                if (e.contains(OpticGeneratorPlugin.TARGET_ANNOTATION)) acc + index else acc
            }

        annotationPositions.map { i ->

            val property = fileContent.elementAt(i + 1)
            val propertyName = property.split(":").first().replace("val", " ").replace("var", " ").replace(" ", "")
            val propertyType = property.split(":").last().split("=").first().replace(",", " ").replace(" ", "")

            var classContext = run {
                var searchedIndex = i

                while (searchedIndex >= 0) {
                    searchedIndex--
                    val suspect = fileContent.elementAt(searchedIndex)
                    if (suspect.contains(DATA_CLASS_IDENTIFIER)) {
                        return@run suspect.split("class").last().split("(").first()
                        break
                    }
                }

                throw Error("Class context not found for $propertyName")
            }

            AnnotatedProperty(
                className = classContext,
                propertyName = propertyName,
                propertyType = propertyType
            )
        }
    }

    // proto content, the generated lenses block
    val proto: String by lazy {
        val groupedAnnotatedProperties = annotatedProperties
            .groupBy { property -> property.className }
            .map { entry -> ProtoObject(entry).protoClass }

        val contentBody = "\n" + groupedAnnotatedProperties.joinToString("\n\n")

        listOf(
            BLOCK_PREPOSSESSOR,
            OpticGeneratorPlugin.SIGNATURE,
            "// Created at: ${System.currentTimeMillis()} Epoch"
        )
            .plus(contentBody)
            .plus(BLOCK_SUBPOSSESSOR)
            .joinToString("\n")
    }

    // new file content where the lenses block is inserted
    val newFileContent: String by lazy {
        val beginIndex = fileContent.indexOfFirst { s -> s.contains(BLOCK_PREPOSSESSOR) }
        val endIndex = fileContent.indexOfFirst { s -> s.contains(BLOCK_SUBPOSSESSOR) }

        val topContent = if (beginIndex != -1) fileContent.take(beginIndex) else fileContent
        val endContent = if (endIndex != -1) fileContent.drop(endIndex + 1) else emptySequence()

        (topContent + proto + endContent)
            .joinToString("\n")
    }

    fun printToFile() {
        source.writeText(newFileContent)
    }
}

class ProtoObject(
    entry: Map.Entry<String, List<AnnotatedProperty>>
) {

    val protoClass by lazy {
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

        listOf("object ${className.uppercaseFirstChar()} {")
            .plus(
                // add indent
                propertyLenses.map { string -> "    $string" }
            )
            .plus("}")
            .joinToString("\n")
    }
}