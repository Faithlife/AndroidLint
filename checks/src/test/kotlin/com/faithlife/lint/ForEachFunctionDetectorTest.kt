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

    fun `test fix forEach implicit parameter`() {
        val code = """
            package looper

            fun loopTester() {
                listOf(1, 2, 3).forEach { println(it) }
            }
        """.trimIndent()

        val fixedCode = """
            Fix for src/looper/test.kt line 4: Replace with language-provided for loops:
            @@ -4 +4
            -     listOf(1, 2, 3).forEach { println(it) }
            +     for (it in listOf(1, 2, 3)) { println(it) }
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectFixDiffs(fixedCode)
    }

    fun `test fix forEach explicit parameter`() {
        val code = """
            package looper

            fun loopTester() {
                listOf(1, 2, 3).forEach { param -> println(param) }
            }
        """.trimIndent()

        val fixedCode = """
            Fix for src/looper/test.kt line 4: Replace with language-provided for loops:
            @@ -4 +4
            -     listOf(1, 2, 3).forEach { param -> println(param) }
            +     for (param in listOf(1, 2, 3)) { println(param) }
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectFixDiffs(fixedCode)
    }

    fun `test fix forEach multiple lines`() {
        val code = """
            package looper

            fun loopTester() {
                listOf(1, 2, 3).forEach {
                    println(it)
                }
            }
        """.trimIndent()

        val fixedCode = """
            Fix for src/looper/test.kt line 4: Replace with language-provided for loops:
            @@ -4 +4
            -     listOf(1, 2, 3).forEach {
            +     for (it in listOf(1, 2, 3)) {
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectFixDiffs(fixedCode)
    }

    fun `test fix forEach multiple lines with parameter`() {
        val code = """
            package looper

            fun loopTester() {
                listOf(1, 2, 3).forEach { param ->
                    println(param)
                }
            }
        """.trimIndent()

        val fixedCode = """
            Fix for src/looper/test.kt line 4: Replace with language-provided for loops:
            @@ -4 +4
            -     listOf(1, 2, 3).forEach { param ->
            +     for (param in listOf(1, 2, 3)) {
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectFixDiffs(fixedCode)
    }

    fun `test fix forEachIndexed`() {
        val code = """
            package looper

            fun loopTester() {
                listOf(1, 2, 3).forEachIndexed { index, i -> println() }
            }
        """.trimIndent()

        val fixedCode = """
            Fix for src/looper/test.kt line 4: Replace with language-provided for loops:
            @@ -4 +4
            -     listOf(1, 2, 3).forEachIndexed { index, i -> println() }
            +     for ((index, i) in listOf(1, 2, 3).withIndex()) { println() }
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectFixDiffs(fixedCode)
    }

    fun `test fix forEachIndexed multiple lines`() {
        val code = """
            package looper

            fun loopTester() {
                listOf(1, 2, 3).forEachIndexed { index, i ->
                    println()
                }
            }
        """.trimIndent()

        val fixedCode = """
            Fix for src/looper/test.kt line 4: Replace with language-provided for loops:
            @@ -4 +4
            -     listOf(1, 2, 3).forEachIndexed { index, i ->
            +     for ((index, i) in listOf(1, 2, 3).withIndex()) {
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectFixDiffs(fixedCode)
    }

    fun `test wrong forEach clean`() {
        val code = """
            package looper

            class Loop : Iterable<Int> {
                fun forEach (i: Int) {}
            }

            fun loopTester() {
                Loop().forEach(1)
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run().expectClean()
    }
}
