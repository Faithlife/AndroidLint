AndroidLint
===========

[![Build Status](https://github.com/Faithlife/AndroidLint/workflows/build/badge.svg)](https://github.com/Faithlife/AndroidLint/actions?workflow=build)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.faithlife/android-lint/badge.svg?gav=true)](https://maven-badges.herokuapp.com/maven-central/com.faithlife/android-lint)

A collection of lint checks for Android the enforce Faithlife house rules.


### Download

via Maven:

```xml
<dependency>
  <groupId>com.faithlife</groupId>
  <artifactId>android-lint</artifactId>
  <version>1.1.3</version>
</dependency>
```

via Gradle:

```kotlin
compileOnly("com.faithlife:android-lint:1.1.3")
```

## Build instructions

This project uses the Gradle build system.

To build the application: `/.gradlew assemble`

To run the tests: `./gradlew test`

To see all available tasks: `./gradlew tasks`
