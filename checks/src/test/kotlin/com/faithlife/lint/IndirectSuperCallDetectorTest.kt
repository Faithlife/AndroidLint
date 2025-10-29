package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestMode

class IndirectSuperCallDetectorTest : LintDetectorTest() {
    override fun getDetector() = IndirectSuperCallDetector()
    override fun getIssues() = listOf(IndirectSuperCallDetector.ISSUE)

    fun `test clean`() {
        val code = """
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

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .run()
            .expectClean()
    }

    fun `test adjacent super call`() {
        val code = """
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

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .skipTestModes(TestMode.JVM_OVERLOADS)
            .run()
            .expectWarningCount(1)
    }

    fun `test adjacent override call`() {
        val code = """
            open class Activity {
                protected open fun onCreate() {}
                protected open fun setTheme() {}
            }

            class SpecialActivity : Activity {
                override fun onCreate() {
                    setTheme()
                }

                override fun setTheme() {

                }
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .run()
            .expectClean()
    }

    fun `test adjacent super call with implicit receiver`() {
        val code = """
            open class Activity {
                protected open fun onCreate() {}
                protected open fun setTheme() {}
            }

            class SpecialActivity : Activity {
                override fun onCreate() {
                    setTheme()
                }
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .run()
            .expectClean()
    }

    fun `test adjacent super call for overload`() {
        val code = """
            open class Activity {
                protected open fun onCreate() {}
                protected open fun onCreate(state: Int) {}
            }

            class SpecialActivity : Activity {
                override fun onCreate() { }
                override fun onCreate(state: Int) {
                    super.onCreate()
                }
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .testModes(TestMode.DEFAULT)
            .run()
            .expectWarningCount(1)
    }

    fun `test non-method call expressions do not crash`() {
        val code = """
            class Activity {
                public void onCreate() {
                    var values = new int[] { 0, 12, 3 };
                }
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(java(code))
            .run()
            .expectClean()
    }
}
