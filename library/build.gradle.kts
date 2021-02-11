plugins {
    id("com.android.library")
    id("maven-publish")
    signing
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
                groupId = "com.faithlife"
                artifactId = "android-lint"
                version = Library.version

                from(components["release"])
            }
        }

        repositories {
            maven {
                credentials {
                    username = properties["ossrhUsername"]?.toString() ?: ""
                    password = properties["ossrhPassword"]?.toString() ?: ""
                }

                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
        }
    }

    signing {
        val signingKeyId = properties["signing.keyid"]?.toString() ?: ""
        val signingKey = properties["signing.key"]?.toString() ?: ""
        val signingPassword = properties["signing.password"]?.toString() ?: ""
        @Suppress("UnstableApiUsage") useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(publishing.publications["release"])
    }
}
