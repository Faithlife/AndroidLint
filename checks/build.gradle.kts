import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

val JAVA_VERSION: String by properties

plugins {
    id("java-library")
    kotlin("jvm")
    id("com.android.lint")
}

repositories {
    google()
}

kotlin {
    sourceSets {
        main {
            this.kotlin.srcDirs("src/main/kotlin")
        }
        test {
            this.kotlin.srcDirs("src/test/kotlin")
        }
    }

    tasks.withType<Test> {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events = setOf(TestLogEvent.SKIPPED, TestLogEvent.PASSED, TestLogEvent.FAILED)
            showStandardStreams = true
        }

        addTestListener(
            object : TestListener {
                override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                    if (suite.parent == null) {
                        val output =
                            "|  Results: ${result.resultType} (${result.testCount} tests, " +
                                "${result.successfulTestCount} passed, " +
                                "${result.failedTestCount} failed, " +
                                "${result.skippedTestCount} skipped)  |"

                        val border = "-".repeat(output.length)
                        println(
                            """
                          |$border
                          |$output
                          |$border
                            """.trimMargin()
                        )
                    }
                }

                override fun afterTest(testDescriptor: TestDescriptor?, result: TestResult?) {}
                override fun beforeTest(testDescriptor: TestDescriptor?) {}
                override fun beforeSuite(suite: TestDescriptor?) {}
            }
        )
    }
}

dependencies {
    compileOnly(lintLibs.bundles.kotlin)
    compileOnly(lintLibs.lint)

    testImplementation(lintLibs.junit)
    testImplementation(lintLibs.bundles.kotlin)
    testImplementation(lintLibs.bundles.lintTest)
}

tasks.named<Jar>("jar").configure {
    manifest.attributes(
        mapOf("Lint-Registry-v2" to "com.faithlife.lint.IssueRegistry")
    )
}

java {
    sourceCompatibility = JavaVersion.toVersion(JAVA_VERSION)
    targetCompatibility = JavaVersion.toVersion(JAVA_VERSION)
}
