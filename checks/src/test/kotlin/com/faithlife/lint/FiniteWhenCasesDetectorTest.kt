package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Assert.*

@Suppress("UnstableApiUsage")
class FiniteWhenCasesDetectorTest : LintDetectorTest() {
    override fun getDetector() = FiniteWhenCasesDetector()
    override fun getIssues() = listOf(FiniteWhenCasesDetector.ISSUE)


}
