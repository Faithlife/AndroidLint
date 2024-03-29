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

    fun `test adjacent override call`() {
        val code = """
            package com.faithlife

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

        lint().files(kotlin(code))
            .run()
            .expectClean()
    }

    fun `test adjacent super call with implicit receiver`() {
        val code = """
            package com.faithlife

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

        lint().files(kotlin(code))
            .run()
            .expectClean()
    }

    fun `test adjacent super call for overload`() {
        val code = """
            package com.faithlife

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

        lint().files(kotlin(code))
            .run()
            .expectWarningCount(1)
    }

    fun `test non-method call expressions do not crash`() {
        val code = """
            package com.faithlife;

            class Activity {
                public void onCreate() {
                    var values = new int[] { 0, 12, 3 };
                }
            }
        """.trimIndent()

        lint().files(java(code))
            .run()
            .expectClean()
    }
}
