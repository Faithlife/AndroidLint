package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest

class SimpleDateFormatDetectorTests : LintDetectorTest() {
    override fun getDetector() = SimpleDateFormatDetector()
    override fun getIssues() = listOf(SimpleDateFormatDetector.ISSUE)

    fun `test SimpleDateFormat detected`() {
        val code = """
            class Dater {
                fun test() {
                    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.ROOT)
                }
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .run()
            .expect(
                """src/Dater.kt:3: Warning: Use Java 8 time APIs instead [SimpleDateFormatDetector]
            |        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.ROOT)
            |                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings
                """.trimMargin(),
            )
    }

    fun `test clean`() {
        val code = """
            class Dater {
                fun test() {
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
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
