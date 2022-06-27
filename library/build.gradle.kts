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
