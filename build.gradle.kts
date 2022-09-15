import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

val VERSION_NAME: String by properties
val JAVA_VERSION: String by properties

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("org.jetbrains.changelog") version "1.3.1"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.3.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${lintLibs.versions.kotlin.get()}")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.20.0")
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
            kotlinOptions.jvmTarget = JAVA_VERSION
            kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"
        }

        withType<JavaCompile>().configureEach {
            sourceCompatibility = JAVA_VERSION
            targetCompatibility = JAVA_VERSION
        }
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<KtlintExtension> {
        version.set("0.45.2")
    }
}
