package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test

class ErrorCatchDetectorTest : LintDetectorTest() {
    override fun getDetector() = ErrorCatchDetector()
    override fun getIssues() = listOf(
        ErrorCatchDetector.ISSUE_CATCH_TOO_GENERIC,
        ErrorCatchDetector.ISSUE_ERROR_CAUGHT,
    )

    @Test
    fun `test clean`() {
        val code = """
            package error

            import java.io.File
            import java.io.FileNotFoundException

            class ErrorHandler {
                fun throwableTester() {
                    val androidDir : File? = try {
                        File("~/.android")
                    } catch (e: FileNotFoundException) {
                        System.err.println(e.message)
                        null
                    }
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run().expectClean()
    }

    @Test
    fun `test throwable catch statement detected`() {
        val code = """
            package error

            import java.io.File

            class ErrorHandler {
                fun throwableTester() {
                    val androidDir : File? = try {
                        File("~/.android")
                    } catch (e: Throwable) {
                        System.err.println(e.message)
                        null
                    }
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run().expectErrorCount(1)
    }

    @Test
    fun `test error catch statement detected`() {
        val code = """
            package error

            import java.io.File

            class ErrorHandler {
                fun throwableTester() {
                    val androidDir : File? = try {
                        File("~/.android")
                    } catch (e: OutOfMemoryError) {
                        System.err.println(e.message)
                        null
                    }
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run().expect(
                """src/error/ErrorHandler.kt:9: Error: Errors should not be caught. [ErrorCatchDetector]
        } catch (e: OutOfMemoryError) {
                    ~~~~~~~~~~~~~~~~
1 errors, 0 warnings""",
            )
    }

    @Test
    fun `test consecutive catch statements detected`() {
        val code = """
            package error

            import java.io.File

            class ErrorHandler {
                fun throwableTester() {
                    val androidDir : File? = try {
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

        lint().files(kotlin(code))
            .run().expectErrorCount(2)
    }

    @Test
    fun `test error multi-catch java statement detected`() {
        val code = """
            package error;

            import java.io.File;

            class ErrorHandler {
                public void throwableTester() {
                    File androidDir = null;
                    try {
                        androidDir = File("~/.android");
                    } catch (OutOfMemoryError | Throwable | Exception e) {
                        System.err.println(e.message);
                    }

                    System.out.println(androidDir != null ? androidDir.getAbsolutePath() : "");
                }
            }
        """.trimIndent()

        lint().files(java(code))
            .run().expect(
                """src/error/ErrorHandler.java:10: Error: Errors should not be caught. [ErrorCatchDetector]
        } catch (OutOfMemoryError | Throwable | Exception e) {
                 ~~~~~~~~~~~~~~~~
src/error/ErrorHandler.java:10: Error: Catching Throwable will include Errors. Be more specific. [ThrowableCatchDetector]
        } catch (OutOfMemoryError | Throwable | Exception e) {
                                    ~~~~~~~~~
2 errors, 0 warnings""",
            )
    }
}
