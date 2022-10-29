package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Severity

@Suppress("UnstableApiUsage")
class ForEachFunctionDetectorTest : LintDetectorTest() {
    override fun getDetector(): Detector = ForEachFunctionDetector()
    override fun getIssues(): MutableList<Issue> = mutableListOf(ForEachFunctionDetector.ISSUE)

    fun `test clean`() {
        val code = """
            package looper

            fun loopTester() {
                for (it in listOf(1, 2, 3)) {
                    println(it)
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run().expectClean()
    }

    fun `test clean nullable forEach receiver`() {
        val code = """
            package looper

            fun loopTester() {
                val list: List<Int>? = if (System.currentTimeMillis() % 2 == 0) listOf(1, 2, 3) else null
                list?.forEach { println(it) }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run().expectClean()
    }

    fun `test forEach detected`() {
        val code = """
            package looper

            fun loopTester() {
                listOf(1, 2, 3).forEach { println(it) }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run().expectCount(1, Severity.INFORMATIONAL)
    }
}
