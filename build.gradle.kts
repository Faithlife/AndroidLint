import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val VERSION_NAME: String by properties
val JAVA_VERSION: String by properties

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("org.jetbrains.changelog") version "1.3.1"
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${lintLibs.versions.kotlin.get()}")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.19.0")
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
        }

        withType<JavaCompile>().configureEach {
            sourceCompatibility = JAVA_VERSION
            targetCompatibility = JAVA_VERSION
        }
    }
}
