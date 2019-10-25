buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:3.5.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50")
        classpath("com.novoda:bintray-release:0.9.1")
    }
}

allprojects {
    repositories {
        jcenter()
        google()
    }
}
