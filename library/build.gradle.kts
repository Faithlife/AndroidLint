plugins {
    id("com.android.library")
    `maven-publish`
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
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
    lintChecks(project(":checks"))
}

publishing {
    publications {
        create<MavenPublication>("myGet") {
            groupId = "com.faithlife.lint"
            artifactId = "android-lint"
            version = "0.1"
            artifact("$buildDir/outputs/aar/library-release.aar")
        }
    }

    repositories {
        maven {
            // change to point to your repo, e.g. http://my.org/repo
            url = uri("$buildDir/repo")
        }
    }
}
