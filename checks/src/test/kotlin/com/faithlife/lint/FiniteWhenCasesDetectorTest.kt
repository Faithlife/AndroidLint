package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest

class FiniteWhenCasesDetectorTest : LintDetectorTest() {
    override fun getDetector() = FiniteWhenCasesDetector()
    override fun getIssues() = listOf(FiniteWhenCasesDetector.ISSUE)

    fun `test check else used in when with enum subject`() {
        val code = """
            enum class MessageKind {
                Text,
                Media,
            }

            data class Message(val id: String, val kind: MessageKind)

            class MessageProcessor {
                private val queue = java.util.ArrayDeque<Message>()
                fun pump() {
                    val message = queue.poll()
                    when (message.kind) {
                       MessageKind.Text -> {}
                       else -> {}
                    }
                }
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .run()
            .expectWarningCount(1)
    }

    fun `test check else used in when with sealed type subject`() {
        val code = """
            sealed interface MessageKind {
                object Text : MessageKind
                object Media : MessageKind
            }

            data class Message(val id: String, val kind: MessageKind)

            class MessageProcessor {
                private val queue = java.util.ArrayDeque<Message>()
                fun pump() {
                    val message = queue.poll()
                    when (message.kind) {
                       is MessageKind.Text -> {}
                       else -> {}
                    }
                }
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .run()
            .expectWarningCount(1)
    }

    fun `test check else used in when with nested sealed type subject`() {
        val code = """
            sealed interface MessageKind {
                object Text : MessageKind
                sealed interface Media : MessageKind {
                    object Video : Media
                    object ImageSet : Media
                }
            }

            data class Message(val id: String, val kind: MessageKind)

            class MessageProcessor {
                private val queue = java.util.ArrayDeque<Message>()
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

        lint()
            .allowMissingSdk()
            .files(kotlin(code))
            .run()
            .expectWarningCount(1)
    }

    fun `test clean framework subject`() {
        val code = """
            data class Message(val id: String, val daySent: java.time.DayOfWeek)

            class MessageProcessor {
                private val queue = java.util.ArrayDeque<Message>()
                fun pump() {
                    val message = queue.poll()
                    when (message.daySent) {
                        DayOfWeek.MONDAY -> {}
                        DayOfWeek.TUESDAY -> {}
                        DayOfWeek.WEDNESDAY -> {}
                        DayOfWeek.THURSDAY -> {}
                        DayOfWeek.FRIDAY -> {}
                        DayOfWeek.SATURDAY -> {}
                        DayOfWeek.SUNDAY -> {}
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

    fun `test clean`() {
        val code = """
            enum class MessageKind {
                Text,
                Media,
            }

            data class Message(val id: String, val kind: MessageKind)

            class MessageProcessor {
                private val queue = java.util.ArrayDeque<Message>()
                fun pump() {
                    val message = queue.poll()
                    when (message.kind) {
                       MessageKind.Text -> {}
                       MessageKind.Media -> {}
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

    @Suppress("IntroduceWhenSubject")
    fun `test clean no subject`() {
        val code = """
            enum class MessageKind {
                Text,
                Media,
            }

            data class Message(val id: String, val kind: MessageKind)

            class MessageProcessor {
                private val queue = java.util.ArrayDeque<Message>()
                fun pump() {
                    val message = queue.poll()
                    when {
                       message.kind == MessageKind.Text -> {}
                       message.kind == MessageKind.Media -> {}
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
}
