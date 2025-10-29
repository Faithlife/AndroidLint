

plugins {
    id("com.android.library")
    id("com.vanniktech.maven.publish")
}

val JAVA_VERSION: String by properties

android {
    namespace = "com.faithlife.lint"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
    }

    testOptions {
        targetSdk = 35
    }

    lint {
        targetSdk = 35
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
