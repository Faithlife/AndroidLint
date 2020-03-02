import java.util.Properties

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
    lintPublish(project(":checks"))
}

// Because the components are created only during the afterEvaluate phase, you must
// configure your publications using the afterEvaluate() lifecycle method.
afterEvaluate {
    publishing {
        publications {
            // Creates a Maven publication called "release".
            create<MavenPublication>("release") {
                groupId = "com.faithlife.lint"
                artifactId = "android-lint"
                version = Library.version

                from(components["release"])
            }
        }

        repositories {
            maven {
                val propertiesFile = file("$rootDir/local.properties")
                val properties = Properties()
                if (propertiesFile.exists()) {
                    properties.load(propertiesFile.inputStream())
                }
                credentials {
                    username = properties.getProperty("bintray.user") ?: ""
                    password = properties.getProperty("bintray.apiKey") ?: ""
                }
                url = uri("https://api.bintray.com/maven/faithlife/maven/android-lint/;publish=1")
            }
        }
    }
}
