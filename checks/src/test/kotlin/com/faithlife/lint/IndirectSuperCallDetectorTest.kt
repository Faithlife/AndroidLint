package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest

class IndirectSuperCallDetectorTest : LintDetectorTest() {
    override fun getDetector() = IndirectSuperCallDetector()
    override fun getIssues() = listOf(IndirectSuperCallDetector.ISSUE)

    fun `test clean`() {
        val code = """
            package com.faithlife

            open class Activity {
                open fun onCreate() {
                }
            }

            class SpecialActivity : Activity {
                override fun onCreate() {
                    super.onCreate()
                    println("This method is kinda useless.")
                }

                private fun setTheme() {
                    println("Setting the theme.")
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectClean()
    }

    fun `test adjacent super call`() {
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
