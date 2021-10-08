include(":checks", ":library")

enableFeaturePreview("VERSION_CATALOGS")

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    versionCatalogs {
        create("libraries") {
            version("kotlin", "1.5.31")
            alias("kotlinStd").to("org.jetbrains.kotlin", "kotlin-stdlib-jdk8").versionRef("kotlin")
            alias("kotlinReflect").to("org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")
            bundle("kotlin", listOf("kotlinStd", "kotlinReflect"))

            version("lint", "30.0.1")
            alias("lintApi").to("com.android.tools.lint", "lint-api").versionRef("lint")

            alias("lint").to("com.android.tools.lint", "lint").versionRef("lint")
            alias("lintTests").to("com.android.tools.lint", "lint-tests").versionRef("lint")
            alias("testUtils").to("com.android.tools", "testutils").versionRef("lint")
            bundle("lintTest", listOf("lint", "lintTests", "testUtils"))

            alias("junit").to("junit", "junit").version("4.13")
        }
    }
}
