AndroidLint
===========

[![Build Status](https://github.com/Faithlife/AndroidLint/workflows/build/badge.svg)](https://github.com/Faithlife/AndroidLint/actions?workflow=build)
[![Maven Central](https://img.shields.io/maven-central/v/com.faithlife/android-lint.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.faithlife%22%20AND%20a:%22android-lint%22)

A collection of lint checks for Android the enforce Faithlife house rules.


### Download

via Maven:

```xml
<dependency>
  <groupId>com.faithlife</groupId>
  <artifactId>android-lint</artifactId>
  <version>1.1.4</version>
</dependency>
```

via Gradle:

```kotlin
compileOnly("com.faithlife:android-lint:1.1.4")
```

## Build instructions

This project uses the Gradle build system.

To build the library: `/.gradlew build`

To see all available tasks: `./gradlew tasks`
