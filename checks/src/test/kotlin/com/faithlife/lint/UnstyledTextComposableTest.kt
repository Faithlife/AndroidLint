package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.faithlife.lint.UnstyledTextComposableDetector.Companion.ISSUE_TEXT_COMPOSABLE_MATERIAL_TYPOGRAPHY
import com.faithlife.lint.UnstyledTextComposableDetector.Companion.ISSUE_TEXT_COMPOSABLE_NO_STYLE

class UnstyledTextComposableTest : LintDetectorTest() {

    override fun getDetector() = UnstyledTextComposableDetector()
    override fun getIssues() = listOf(ISSUE_TEXT_COMPOSABLE_NO_STYLE, ISSUE_TEXT_COMPOSABLE_MATERIAL_TYPOGRAPHY)

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

    fun `test warning null parameter`() {
        lint()
            .allowMissingSdk()
            .files(
                *COMPOSE_STUBS,
                kotlin(
                    """
                @androidx.compose.runtime.Composable
                fun someUI() {
                    androidx.compose.material3.Text("Hello", style = null)
                }
                """,
                ),
            )
            .run()
            .expect(
                """
src/test.kt:4: Warning: Text() composable is missing a style parameter [UnstyledTextComposable]
                    androidx.compose.material3.Text("Hello", style = null)
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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

    fun `test clean style in a variable`() {
        lint()
            .allowMissingSdk()
            .files(
                *COMPOSE_STUBS,
                kotlin(
                    """
                @androidx.compose.runtime.Composable
                fun someUI() {
                    val textStyle = androidx.compose.ui.text.TextStyle()
                    androidx.compose.material3.Text("OK", style = textStyle)
                }
                """,
                ),
            )
            .run()
            .expectClean()
    }

    fun `test clean nullable style`() {
        lint()
            .allowMissingSdk()
            .files(
                *COMPOSE_STUBS,
                kotlin(
                    """
                @androidx.compose.runtime.Composable
                fun someUI() {
                    val textStyle: androidx.compose.ui.text.TextStyle? = androidx.compose.ui.text.TextStyle()
                    androidx.compose.material3.Text("OK", style = textStyle)
                }
                """,
                ),
            )
            .run()
            .expectClean()
    }

    fun `test warning null style in variable`() {
        lint()
            .allowMissingSdk()
            .files(
                *COMPOSE_STUBS,
                kotlin(
                    """
                @androidx.compose.runtime.Composable
                fun someUI() {
                    val textStyle: androidx.compose.ui.text.TextStyle? = null
                    androidx.compose.material3.Text("OK", style = textStyle)
                }
                """,
                ),
            )
            .run()
            .expect(
                """
src/test.kt:5: Warning: Text() composable is missing a style parameter [UnstyledTextComposable]
                    androidx.compose.material3.Text("OK", style = textStyle)
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warning
                """.trimIndent(),
            )
    }

    fun `test warning material typography`() {
        lint()
            .allowMissingSdk()
            .files(
                *COMPOSE_STUBS,
                kotlin(
                    """
                @androidx.compose.runtime.Composable
                fun someUI() {
                    androidx.compose.material3.Text(
                        "OK",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                    )
                }
                """,
                ),
            )
            .run()
            .expect(
                """
src/test.kt:6: Warning: Do not use MaterialTheme.typography for Text styles. [Material3Typography]
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warning
                """.trimIndent(),
            )
    }

    fun `test custom typography clean`() {
        lint()
            .allowMissingSdk()
            .files(
                *COMPOSE_STUBS,
                kotlin(
                    """
                @androidx.compose.runtime.Composable
                fun someUI() {
                    androidx.compose.material3.Text(
                        "OK",
                        style = some.other.place.TextStyles.genericText
                    )
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

                object MaterialTheme {
                    object typography {
                        val bodyLarge = androidx.compose.ui.text.TextStyle()
                    }
                }
                """,
            ),
            kotlin(
                """
                package some.other.place
                @Suppress("TestFunctionName")
                fun Text(text: String) {}

                object TextStyles {
                    val genericText = androidx.compose.ui.text.TextStyle()
                }
                """,
            ),
        )
    }
}
