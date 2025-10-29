package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest

class ObsoleteDateInstantiationDetectorTests : LintDetectorTest() {
    override fun getDetector() = ObsoleteDateInstantiationDetector()
    override fun getIssues() = listOf(ObsoleteDateInstantiationDetector.ISSUE)

    fun `test Date detected`() {
        val code = """
            class Dater {
                fun test() {
                    val formatter = java.util.Date(1244)
                }
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .run()
            .expect(
                """src/Dater.kt:3: Warning: Use Java 8 time APIs instead [ObsoleteDateInstantiationDetector]
            |        val formatter = java.util.Date(1244)
            |                        ~~~~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings
                """.trimMargin(),
            )
    }

    fun `test clean`() {
        val code = """
            class Dater {
                fun test() {
                    val formatter = Instant.ofEpochMilli(1244)
                }
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .run()
            .expectClean()
    }
}
