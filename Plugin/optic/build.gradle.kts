plugins {
    `kotlin-dsl`
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.gradle.plugin-publish") version "1.3.0"
}

version = "1.1.2"
group = "io.github.stefanusayudha"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    implementation(libs.kotlinx.serialization.json)
}

gradlePlugin {
    website.set("https://github.com/stefanusayudha/OpticPlugin/")
    vcsUrl.set("https://github.com/stefanusayudha/OpticPlugin/")
    plugins {
        register("OpticGeneratorPlugin") {
            id = "io.github.stefanusayudha.OpticGeneratorPlugin"
            implementationClass = "plugin.optic.OpticGeneratorPlugin"
            displayName = "Optic Generator"
            description = "Plugin to generate optical object such Lenses etc."
            tags.set(listOf("kmm", "lens", "generator", "optic"))
        }
    }
}