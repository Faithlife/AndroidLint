# Changelog

## 2.0.0 - 2025-10-30
### Added
- Detect incorrect styling of `Text` composables.

### Changed
- Breaking: Updated minSdk to 30

## 1.2.0 - 2022-11-22
### Added
- `RedundantCoroutineScopeDetector` warns when a `androidx.lifecycle.LifecycleOwner`, `androidx.lifecycle.ViewModel`, or `android.view.View` implements or has a field assignable to `kotlinx.coroutines.CoroutineScope`.
  Existing scopes are bound to relevant lifecycle events in the system and will prevent coroutines from running past their utility.
  - `LifecycleOwner` should use `lifecycleScope`
  - `Fragment` should use `viewLifecycleOwner.lifecycleScope`,
  - `ViewModel` should use `viewModelScope`
  - `View` should use `findViewTreeLifecycleOwner()?.lifecycleScope`
- `FiniteWhenCasesDetector` warns when `else` is used as a `when` branch when the `when` subject has finite possibilities
  - This detector works best when applied to an app project with `lint.checkDependencies = true` in the app module AGP DSL.
- `ForEachFunctionDetector` reports `forEach` and `forEachIndexed` use and encourages a language for loop replacement
- `SkippedClassLocalOverrideDetector` warns when an explicit super method is called outside of the corresponding override.
- `ErrorCatchDetector` reports an error when a catch block might catch a `java.lang.Error` type.

### Changed
- Updated build tooling
- Treat lint warnings as errors for issues in this library
- Add spotless

## 1.1.6 - 2022-03-11
### Changed
- Java version is increased to Java 11

### Fixed
- Multiple build variants are published after upgrading gradle maven publish plugin to [0.19.0](https://github.com/vanniktech/gradle-maven-publish-plugin/blob/master/CHANGELOG.md#version-0190-2022-02-26)
- A NPE in SingleApostropheDetector for atypical resource definitions

## 1.1.5 - 2022-03-04
### Added
- Changelog automation

### Changed
- Issues related to Java 8 time APIs (java.time) are warnings instead of errors

### Fixed
- Updated compile (31), target (31), and minimum sdk (25)
- Use of period at the end of a lint message
- Updated development tools

## 1.1.4 - 2021-10-08
### Added
- A vendor to the lint registry

### Changed
- Update build tools
- Simplified maven publishing
- Run lint against the lint library

## 1.1.3 - 2021-02-21
### Changed
- Compile against SDK 30
- Update the Android Gradle Plugin
- Publish to sonatype nexus

## 1.0.1 - 2019-11-15
### Fixed
- An issue parsing escaped apostrophes
