package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest

class UnstyledTextComposableTest : LintDetectorTest() {

    override fun getDetector() = UnstyledTextComposableDetector()
    override fun getIssues() = listOf(ISSUE_TEXT_COMPOSABLE_NO_STYLE)

    fun `test clean`() {
        lint()
            .allowMissingSdk()
            .files(
                *COMPOSE_STUBS,
                kotlin(
                    """
                @androidx.compose.runtime.Composable
                fun someUI() {
                    androidx.compose.material3.Text("OK", style = androidx.compose.ui.text.TextStyle())
                }
                """,
                ),
            )
            .run()
            .expectClean()
    }

    fun `test missing style parameter`() {
        lint()
            .allowMissingSdk()
            .files(
                *COMPOSE_STUBS,
                kotlin(
                    """
                @androidx.compose.runtime.Composable
                fun someUI() {
                    androidx.compose.material3.Text("Hello")
                }
                """,
                ),
            )
            .run()
            .expect(
                """
src/test.kt:4: Warning: Text() composable is missing a style parameter [UnstyledTextComposable]
                    androidx.compose.material3.Text("Hello")
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warning
        """,
            )
    }

    fun `test ignore other packages besides compose`() {
        lint()
            .allowMissingSdk()
            .files(
                *COMPOSE_STUBS,
                kotlin(
                    """
                fun doSomething() {
                    some.other.place.Text("Hello")
                }
                """,
                ),
            )
            .run()
            .expectClean()
    }

    private companion object {
        val COMPOSE_STUBS = arrayOf(
            kotlin(
                """
                package androidx.compose.runtime
                annotation class Composable
                """,
            ),
            kotlin(
                """
                package androidx.compose.ui.text
                class TextStyle
                """,
            ),
            kotlin(
                """
                package androidx.compose.material3

                import androidx.compose.runtime.Composable

                @Composable
                @Suppress("TestFunctionName")
                fun Text(text: String, style: androidx.compose.ui.text.TextStyle? = null) {}
                """,
            ),
            kotlin(
                """
                package some.other.place
                @Suppress("TestFunctionName")
                fun Text(text: String) {}
                """,
            ),
        )
    }
}
