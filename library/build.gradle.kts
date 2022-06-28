import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    id("com.vanniktech.maven.publish")
}

val JAVA_VERSION: String by properties

android {
    compileSdk = 32

    defaultConfig {
        minSdk = 25
        targetSdk = 32
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(JAVA_VERSION)
        targetCompatibility = JavaVersion.toVersion(JAVA_VERSION)
    }

    lint {
        warningsAsErrors = true
        checkDependencies = true

        // The pre-release AGP 7.3's lint checks require 33, but the AGP release was only tested
        // with 32, so that causes another lint error. We should use the most up to date version
        // when AGP 7.3 stabilizes and remove this supression.
        disable += "OldTargetApi"
    }
}

repositories {
    google()
}

dependencies {
    implementation(project(":checks"))
    lintPublish(project(":checks"))
}

mavenPublishing {
    signAllPublications()
    publishToMavenCentral(SonatypeHost.DEFAULT)
}

mavenPublish {
    androidVariantToPublish = "release"
}
