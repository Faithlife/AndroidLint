package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test

@Suppress("UnstableApiUsage")
class FiniteWhenCasesDetectorTest : LintDetectorTest() {
    override fun getDetector() = FiniteWhenCasesDetector()
    override fun getIssues() = listOf(FiniteWhenCasesDetector.ISSUE)

    @Test
    fun `test check else used in when with enum subject`() {
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
                       MessageKind.Text -> {}
                       else -> {}
                    }
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun `test check else used in when with sealed type subject`() {
        val code = """
            package com.faithlife

            import java.util.ArrayDeque

            sealed interface MessageKind {
                object Text : MessageKind
                object Media : MessageKind
            }

            data class Message(val id: String, val kind: MessageKind)

            class MessageProcessor {
                private val queue = ArrayDeque<Message>()
                fun pump() {
                    val message = queue.poll()
                    when (message.kind) {
                       is MessageKind.Text -> {}
                       else -> {}
                    }
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun `test check else used in when with nested sealed type subject`() {
        val code = """
            package com.faithlife

            import java.util.ArrayDeque

            sealed interface MessageKind {
                object Text : MessageKind
                sealed interface Media : MessageKind {
                    object Video : Media
                    object ImageSet : Media
                }
            }

            data class Message(val id: String, val kind: MessageKind)

            class MessageProcessor {
                private val queue = ArrayDeque<Message>()
                fun pump() {
                    val message = queue.poll()
                    val kind = message.kind as MessageKind.Media
                    when (kind) {
                       is MessageKind.Media.Video -> {}
                       else -> {}
                    }
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun `test clean`() {
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
                       MessageKind.Text -> {}
                       MessageKind.Media -> {}
                    }
                }
            }
        """.trimIndent()

        lint().files(kotlin(code))
            .run()
            .expectClean()
    }
}
