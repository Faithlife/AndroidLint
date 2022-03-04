include(":checks", ":library")

dependencyResolutionManagement {
    versionCatalogs {
        create("lintLibs") {
            version("kotlin", "1.6.10")
            library("kotlinStd", "org.jetbrains.kotlin", "kotlin-stdlib-jdk8").versionRef("kotlin")
            library("kotlinReflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")
            bundle("kotlin", listOf("kotlinStd", "kotlinReflect"))

            version("lint", "30.1.2")
            library("lintApi", "com.android.tools.lint", "lint-api").versionRef("lint")

            library("lint", "com.android.tools.lint", "lint").versionRef("lint")
            library("lintTests", "com.android.tools.lint", "lint-tests").versionRef("lint")
            library("testUtils", "com.android.tools", "testutils").versionRef("lint")
            bundle("lintTest", listOf("lint", "lintTests", "testUtils"))

            version("junit", "4.13.2")
            library("junit", "junit", "junit").versionRef("junit")
        }
    }
}
