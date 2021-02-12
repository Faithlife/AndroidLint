import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java-library")
    kotlin("jvm")
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
    }
}

dependencies {
    compileOnly("com.android.tools.lint:lint-api:$lintVersion")
    compileOnly("com.android.tools.lint:lint-checks:$lintVersion")
    testImplementation("junit:junit:4.13.1")
    testImplementation("com.android.tools.lint:lint:$lintVersion")
    testImplementation("com.android.tools.lint:lint-tests:$lintVersion")
    testImplementation("com.android.tools:testutils:$lintVersion")
}

tasks.jar {
    @Suppress("UnstableApiUsage")
    manifest.attributes(
        mapOf("Lint-Registry-v2" to "com.faithlife.lint.IssueRegistry")
    )
}
