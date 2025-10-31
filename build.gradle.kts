import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val VERSION_NAME: String by properties
val JAVA_VERSION: String by properties

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("org.jetbrains.changelog") version "2.4.0"
    id("com.diffplug.spotless") version "6.11.0" apply false
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.13.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${lintLibs.versions.kotlin.get()}")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.22.0")
    }
}

version = VERSION_NAME

changelog {
    path.set("$rootDir/changelog.md")
    version.set(VERSION_NAME)
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }

    tasks {
        withType<KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(JAVA_VERSION))
                freeCompilerArgs.add("-Xcontext-receivers")
            }
        }

        withType<JavaCompile>().configureEach {
            sourceCompatibility = JAVA_VERSION
            targetCompatibility = JAVA_VERSION
        }
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    configure<SpotlessExtension> {
        kotlin {
            ktlint("0.47.1")
                // Spotless doesn't respect .editorconfig yet.
                //   https://github.com/diffplug/spotless/issues/142
                .editorConfigOverride(
                    mapOf(
                        "charset" to "utf-8",
                        "end_of_line" to "lf",
                        "trim_trailing_whitespace" to true,
                        "insert_final_newline" to true,
                        "indent_style" to "space",
                        "indent_size" to 4,
                        "ij_kotlin_allow_trailing_comma" to true,
                        "ij_kotlin_allow_trailing_comma_on_call_site" to true
                    )
                )
        }

        kotlinGradle {
            ktlint("0.47.1")
                // Spotless doesn't respect .editorconfig yet.
                //   https://github.com/diffplug/spotless/issues/142
                .editorConfigOverride(
                    mapOf(
                        "charset" to "utf-8",
                        "end_of_line" to "lf",
                        "trim_trailing_whitespace" to true,
                        "insert_final_newline" to true,
                        "indent_style" to "space",
                        "indent_size" to 4,
                        "ij_kotlin_allow_trailing_comma" to true,
                        "ij_kotlin_allow_trailing_comma_on_call_site" to true
                    )
                )
        }
    }
}
