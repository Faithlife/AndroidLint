buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libraries.versions.kotlin.get()}")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.18.0")
    }
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
