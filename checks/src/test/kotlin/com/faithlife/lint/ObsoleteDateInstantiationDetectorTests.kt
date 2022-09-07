package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test

@Suppress("UnstableApiUsage")
class ObsoleteDateInstantiationDetectorTests : LintDetectorTest() {
    override fun getDetector() = ObsoleteDateInstantiationDetector()
    override fun getIssues() = listOf(ObsoleteDateInstantiationDetector.ISSUE)

    @Test
    fun `instantiated Date detected`() {
        val code = """
            package com.faithlife

            import java.util.Date

            class Dater {
                fun test() {
                    val formatter = Date(1244)
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expect(
                """src/com/faithlife/Dater.kt:7: Warning: Use Java 8 time APIs instead [ObsoleteDateInstantiationDetector]
            |        val formatter = Date(1244)
            |                        ~~~~~~~~~~
            |0 errors, 1 warnings
                """.trimMargin()
            )
    }

    @Test
    fun `check clean`() {
        val code = """
            package com.faithlife

            import java.time.Instant

            class Dater {
                fun test() {
                    val formatter = Instant.ofEpochMilli(1244)
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectClean()
    }
}
