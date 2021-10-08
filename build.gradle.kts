import io.codearte.gradle.nexus.NexusStagingExtension

buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libraries.versions.kotlin.get()}")

        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
        classpath("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.22.0")
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

apply(plugin = "io.codearte.nexus-staging")
configure<NexusStagingExtension> {
    packageGroup = "com.faithlife"
    stagingProfileId = System.getenv("SONATYPE_PROFILE_ID")
    numberOfRetries = 60
    delayBetweenRetriesInMillis = 30_000
    username = System.getenv("OSSRH_USERNAME")
    password = System.getenv("OSSRH_PASSWORD")
}
