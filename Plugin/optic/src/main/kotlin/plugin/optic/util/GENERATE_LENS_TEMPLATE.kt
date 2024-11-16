package plugin.optic.util

val GENERATE_LENS_TEMPLATE = """
    package io.github.stefanusayudha.optic

    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.SOURCE)
    annotation class GenerateLens
    
""".trimIndent()