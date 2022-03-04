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
        classpath("com.android.tools.build:gradle:7.1.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${lintLibs.versions.kotlin.get()}")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.19.0")
    }
}

version = properties("VERSION_NAME")

changelog {
    path.set("$rootDir/changelog.md")
    version.set(properties("VERSION_NAME"))
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }

    tasks {
        val targetJavaVersion = JavaVersion.VERSION_1_8.toString()

        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions.jvmTarget = targetJavaVersion
        }

        withType<JavaCompile> {
            sourceCompatibility = targetJavaVersion
            targetCompatibility = targetJavaVersion
        }
    }
}

ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_KEY_FILE_PATH")
