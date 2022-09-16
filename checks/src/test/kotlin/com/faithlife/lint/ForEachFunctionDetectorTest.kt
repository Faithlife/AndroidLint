package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Severity

@Suppress("UnstableApiUsage")
class ForEachFunctionDetectorTest : LintDetectorTest() {
    override fun getDetector(): Detector = ForEachFunctionDetector()
    override fun getIssues(): MutableList<Issue> = mutableListOf(ForEachFunctionDetector.ISSUE)

    fun `test forEach detected`() {
        val code = """
            package looper

            fun loopTester() {
                listOf(1, 2, 3).forEach { println(it) }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run().expectCount(1, Severity.WARNING)
    }
}
