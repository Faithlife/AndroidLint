package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test

class SimpleDateFormatDetectorTests : LintDetectorTest() {
    override fun getDetector() = SimpleDateFormatDetector()
    override fun getIssues() = listOf(SimpleDateFormatDetector.ISSUE)

    @Test
    fun `test SimpleDateFormat detected`() {
        val code = """
            package com.faithlife

            import java.text.SimpleDateFormat
            import java.util.Locale

            class Dater {
                fun test() {
                    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expect(
                """src/com/faithlife/Dater.kt:8: Warning: Use Java 8 time APIs instead [SimpleDateFormatDetector]
            |        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)
            |                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings
                """.trimMargin(),
            )
    }

    @Test
    fun `test clean`() {
        val code = """
            package com.faithlife

            import java.time.format.DateTimeFormatter

            class Dater {
                fun test() {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectClean()
    }
}
