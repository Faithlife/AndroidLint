# Changelog

## Unreleased

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## 1.2.0 - 2022-11-22

### Added
- RedundantCoroutineScopeDetectorandroidx.lifecycle.LifecycleOwnerandroidx.lifecycle.ViewModelandroid.view.Viewkotlinx.coroutines.CoroutineScopeLifecycleOwnerlifecycleScopeFragmentviewLifecycleOwner.lifecycleScopeViewModelviewModelScopeViewfindViewTreeLifecycleOwner()?.lifecycleScopeFiniteWhenCasesDetectorelsewhenwhenlint.checkDependencies = trueForEachFunctionDetectorforEachforEachIndexedSkippedClassLocalOverrideDetectorErrorCatchDetectorjava.lang.Error

## 1.1.6

### Changed
- Java version is increased to Java 11

### Fixed
- Multiple build variants are published after upgrading gradle maven publish plugin to [0.19.0](https://github.com/vanniktech/gradle-maven-publish-plugin/blob/master/CHANGELOG.md#version-0190-2022-02-26)
- A NPE in SingleApostropheDetector for atypical resource definitions

## 1.1.5

### Added
- Changelog automation

### Changed
- Issues related to Java 8 time APIs (java.time) are warnings instead of errors

### Fixed
- Updated compile (31), target (31), and minimum sdk (25)
- Use of period at the end of a lint message
- Updated development tools

## 1.1.4

### Added
- A vendor to the lint registry

### Changed
- Update build tools
- Simplified maven publishing
- Run lint against the lint library

## 1.1.3

### Changed
- Compile against SDK 30
- Update the Android Gradle Plugin
- Publish to sonatype nexus

## 1.0.1

### Fixed
- An issue parsing escaped apostrophes
