AndroidLint
===========

[![Build Status](https://github.com/Faithlife/AndroidLint/workflows/build/badge.svg)](https://github.com/Faithlife/AndroidLint/actions?workflow=build)
[![Download](https://api.bintray.com/packages/faithlife/maven/android-lint/images/download.svg)](https://bintray.com/faithlife/maven/android-lint/_latestVersion)

A collection of lint checks for Android the enforce Faithlife house rules.


### Download

via Maven:

```xml
<dependency>
  <groupId>com.faithlife.lint</groupId>
  <artifactId>android-lint</artifactId>
  <version>1.1.2</version>
</dependency>
```

via Gradle:

```kotlin
compileOnly("com.faithlife.lint:android-lint:1.1.2")
```

## Build instructions

This project uses the Gradle build system.

To build the application: `/.gradlew assemble`

To run the tests: `./gradlew test`

To see all available tasks: `./gradlew tasks`
