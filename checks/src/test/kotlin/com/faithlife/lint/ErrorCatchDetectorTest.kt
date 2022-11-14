package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test

class ErrorCatchDetectorTest : LintDetectorTest() {
    override fun getDetector() = ErrorCatchDetector()
    override fun getIssues() = listOf(ErrorCatchDetector.ISSUE)

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
            .run().expectErrorCount(1)
    }
}
