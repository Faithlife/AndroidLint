package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import junit.framework.TestCase

class IndirectSuperCallDetectorTest : LintDetectorTest() {
    override fun getDetector() = IndirectSuperCallDetector()
    override fun getIssues() = listOf(IndirectSuperCallDetector.ISSUE)

    fun `test foo`() {
        val code = """
            package com.faithlife

            open class Activity {
                open fun onCreate() {
                }
            }

            class SpecialActivity : Activity {
                override fun onCreate() {
                    setTheme()
                }

                private fun setTheme() {
                    super.onCreate()
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectWarningCount(1)
    }
}
