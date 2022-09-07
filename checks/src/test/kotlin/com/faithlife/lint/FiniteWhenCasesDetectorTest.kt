package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Assert.*
import org.junit.Test

@Suppress("UnstableApiUsage")
class FiniteWhenCasesDetectorTest : LintDetectorTest() {
    override fun getDetector() = FiniteWhenCasesDetector()
    override fun getIssues() = listOf(FiniteWhenCasesDetector.ISSUE)

    @Test
    fun `test check bad`() {
        val code = """
            package com.faithlife

            import java.util.ArrayDeque

            enum class MessageKind {
                Text,
                Media,
            }

            data class Message(val id: String, val kind: MessageKind)

            class MessageProcessor {
                private val queue = ArrayDeque<Message>()
                fun pump() {
                    val message = queue.poll()
                    when (message.kind) {
                       MessageKind.TEXT -> {}
                       else -> {}
                    }
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectWarningCount(1)
    }
}
