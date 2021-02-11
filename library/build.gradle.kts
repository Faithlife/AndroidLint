import org.jetbrains.kotlin.js.inline.util.getSimpleName

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

                @Suppress("UnstableApiUsage")
                pom {
                    name.set("android-lint")
                    description.set("A collection of lint checks for Android the enforce Faithlife house rules.")
                    url.set("https://github.com/Faithlife/AndroidLint/")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://github.com/Faithlife/AndroidLint/blob/master/LICENSE")
                        }
                    }

                    developers {
                        developer {
                            id.set("jzbrooks")
                            name.set("Justin Brooks")
                            email.set("justin.brooks@faithlife.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:github.com/Faithlife/AndroidLint.git")
                        developerConnection.set("scm:git:ssh://github.com/Faithlife/AndroidLint.git")
                        url.set("https://github.com/Faithlife/AndroidLint/tree/master")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "sonatype"
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = properties["ossrhUsername"]?.toString() ?: ""
                    password = properties["ossrhPassword"]?.toString() ?: ""
                }
            }
        }
    }

    signing {
        sign(publishing.publications)
    }
}
