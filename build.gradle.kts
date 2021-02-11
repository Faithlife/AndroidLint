import io.codearte.gradle.nexus.NexusStagingExtension

buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:3.6.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
        classpath("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.22.0")
    }
}

allprojects {
    repositories {
        jcenter()
        google()
    }
}

apply(plugin = "io.codearte.nexus-staging")
configure<NexusStagingExtension> {
    packageGroup = "com.faithlife"
    stagingProfileId = properties["sonatypeProfileId"]?.toString() ?: ""
    username = properties["ossrhUsername"]?.toString() ?: ""
    password = properties["ossrhPassword"]?.toString() ?: ""
}
