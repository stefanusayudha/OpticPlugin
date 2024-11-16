package plugin.optic

import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.io.File
import kotlin.text.contains
import kotlin.text.split

class LensBuilder(
    val source: File
) {
    companion object {
        const val DATA_CLASS_IDENTIFIER = "data class"
    }

    val content: String
        get() {
            val annotatedProperties = getAnnotatedProperties(source)
            val groupedAnnotatedProperties = annotatedProperties
                .groupBy { property -> property.className }
                .map { entry -> toProtoClass(entry) }
            val contentBody =
                groupedAnnotatedProperties.foldIndexed("") { index, acc, e -> if (index != 0) "$acc\n\n$e" else "$acc\n$e" }

            return listOf(OpticGeneratorPlugin.BLOCK_PREPOSSESSOR, OpticGeneratorPlugin.SIGNATURE)
                .plus(contentBody)
                .plus(OpticGeneratorPlugin.BLOCK_SUBPOSSESSOR)
                .foldIndexed("") { index, acc, n ->
                    if (index != 0) "$acc\n$n" else "$acc$n"
                }
        }

    private fun getAnnotatedProperties(file: File): List<AnnotatedProperty> {
        return file.useLines { lines ->
            val content = lines.toList()
            val annotationPositions =
                content.foldIndexed(listOf<Int>()) { index, acc, e ->
                    if (e.contains(OpticGeneratorPlugin.TARGET_ANNOTATION)) acc + index else acc
                }

            annotationPositions.map { i ->

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
}