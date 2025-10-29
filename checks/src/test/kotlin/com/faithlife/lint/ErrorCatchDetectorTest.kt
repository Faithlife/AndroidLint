package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest

class ErrorCatchDetectorTest : LintDetectorTest() {
    override fun getDetector() = ErrorCatchDetector()
    override fun getIssues() = listOf(
        ErrorCatchDetector.ISSUE_CATCH_TOO_GENERIC,
        ErrorCatchDetector.ISSUE_ERROR_CAUGHT,
    )

    fun `test clean`() {
        val code = """
            class ErrorHandler {
                fun throwableTester() {
                    val androidDir : java.io.File? = try {
                        File("~/.android")
                    } catch (e: java.io.FileNotFoundException) {
                        System.err.println(e.message)
                        null
                    }
                }
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .run()
            .expectClean()
    }

    fun `test throwable catch statement detected`() {
        val code = """
            class ErrorHandler {
                fun throwableTester() {
                    val androidDir : java.io.File? = try {
                        File("~/.android")
                    } catch (e: Throwable) {
                        System.err.println(e.message)
                        null
                    }
                }
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .run()
            .expectErrorCount(1)
    }

    fun `test error catch statement detected`() {
        val code = """
            class ErrorHandler {
                fun throwableTester() {
                    val androidDir : java.io.File? = try {
                        File("~/.android")
                    } catch (e: OutOfMemoryError) {
                        System.err.println(e.message)
                        null
                    }
                }
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .run()
            .expect(
                """src/ErrorHandler.kt:5: Error: Errors should not be caught. [ErrorCatchDetector]
        } catch (e: OutOfMemoryError) {
                    ~~~~~~~~~~~~~~~~
1 errors, 0 warnings""",
            )
    }

    fun `test consecutive catch statements detected`() {
        val code = """
            class ErrorHandler {
                fun throwableTester() {
                    val androidDir : java.io.File? = try {
                        File("~/.android")
                    } catch (e: OutOfMemoryError) {
                        System.err.println(e.message)
                        null
                    } catch (t: Throwable) {
                        System.err.println(e.message + " was thrown")
                        null
                    }
                }
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .run()
            .expectErrorCount(2)
    }

    fun `test error multi-catch java statement detected`() {
        val code = """
            class ErrorHandler {
                public void throwableTester() {
                    File androidDir = null;
                    try {
                        androidDir = java.io.File("~/.android");
                    } catch (OutOfMemoryError | Throwable | Exception e) {
                        System.err.println(e.message);
                    }

                    System.out.println(androidDir != null ? androidDir.getAbsolutePath() : "");
                }
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(java(code))
            .run()
            .expect(
                """src/ErrorHandler.java:6: Error: Errors should not be caught. [ErrorCatchDetector]
        } catch (OutOfMemoryError | Throwable | Exception e) {
                 ~~~~~~~~~~~~~~~~
src/ErrorHandler.java:6: Error: Catching Throwable will include Errors. Be more specific. [ThrowableCatchDetector]
        } catch (OutOfMemoryError | Throwable | Exception e) {
                                    ~~~~~~~~~
2 errors, 0 warnings""",
            )
    }
}
