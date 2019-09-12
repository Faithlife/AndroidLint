import com.novoda.gradle.release.PublishExtension
import java.util.Properties

plugins {
    id("com.android.library")
    id("com.novoda.bintray-release")
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

configure<PublishExtension> {
    val properties = Properties()
    properties.load(file("$rootDir/local.properties").inputStream())

    bintrayUser = properties.getProperty("bintray.user")
    bintrayKey = properties.getProperty("bintray.apiKey")
    userOrg = "faithlife"
    repoName = "maven"
    groupId = "com.faithlife.lint"
    artifactId = "android-lint"
    publishVersion = Library.version
    desc = "Android Lint checks to enforce Faithlife house rules"
    website = "https://github.com/Faithlife/AndroidLint"

    dryRun = false
}
