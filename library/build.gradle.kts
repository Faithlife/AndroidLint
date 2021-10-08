import org.jetbrains.kotlin.js.inline.util.getSimpleName

plugins {
    id("com.android.library")
    id("com.vanniktech.maven.publish")
}

android {
    compileSdk = 30

    defaultConfig {
        minSdk = 23
        targetSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

repositories {
    google()
}

dependencies {
    implementation(project(":checks"))
    lintPublish(project(":checks"))
}
