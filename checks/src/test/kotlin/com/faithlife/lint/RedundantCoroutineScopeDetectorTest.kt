package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest

class RedundantCoroutineScopeDetectorTest : LintDetectorTest() {
    override fun getDetector() = RedundantCoroutineScopeDetector()
    override fun getIssues() = listOf(RedundantCoroutineScopeDetector.ISSUE)

    fun `test clean`() {
        val cleanCode = """
            class ProfileFragment : androidx.fragment.app.Fragment()
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(kotlin(cleanCode), java(FRAGMENT_JAVA_STUB), java(LIFECYCLE_JAVA_STUB))
            .run()
            .expectClean()
    }

    fun `test classes without associated scopes can implement CoroutineScope`() {
        val cleanCode = """
            class ProfilePresenter : com.faithlife.CoroutineScopeBase()
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(
                kotlin(cleanCode),
                kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
                kotlin(COROUTINE_SCOPE_KT_STUB),
            )
            .run()
            .expectClean()
    }

    fun `test classes without associated scopes can have CoroutineScope fields`() {
        val cleanCode = """
            class ProfilePresenter {
                private val coroutineScope = com.faithlife.CoroutineScopeBase()
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(
                kotlin(cleanCode),
                kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
                kotlin(COROUTINE_SCOPE_KT_STUB),
            )
            .run()
            .expectClean()
    }

    fun `test LifecycleOwner should not implement CoroutineScope`() {
        val problematicCode = """
            class ProfileFragment : androidx.fragment.app.Fragment(), kotlinx.coroutines.CoroutineScope
        """.trimIndent()

        val result = lint()
            .allowMissingSdk()
            .files(
                java(FRAGMENT_JAVA_STUB),
                java(LIFECYCLE_JAVA_STUB),
                kotlin(COROUTINE_SCOPE_KT_STUB),
                kotlin(problematicCode),
            ).run()

        result.expect(
            """src/ProfileFragment.kt:1: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
class ProfileFragment : androidx.fragment.app.Fragment(), kotlinx.coroutines.CoroutineScope
                                                          ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    fun `test LifecycleOwner should not extend CoroutineScope implementation`() {
        val problematicCode = """
            class ProfileFragment : com.faithlife.CoroutineScopeBase(), androidx.lifecycle.LifecycleOwner
        """.trimIndent()

        val result = lint()
            .allowMissingSdk()
            .files(
                java(LIFECYCLE_JAVA_STUB),
                kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
                kotlin(COROUTINE_SCOPE_KT_STUB),
                kotlin(problematicCode),
            ).run()

        result.expect(
            """src/ProfileFragment.kt:1: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
class ProfileFragment : com.faithlife.CoroutineScopeBase(), androidx.lifecycle.LifecycleOwner
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    fun `test ViewModel should not implement CoroutineScope`() {
        val problematicCode = """
            class ProfileViewModel : androidx.lifecycle.ViewModel(), kotlinx.coroutines.CoroutineScope
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(
                java(VIEW_MODEL_JAVA_STUB),
                kotlin(COROUTINE_SCOPE_KT_STUB),
                kotlin(problematicCode),
            )
            .run()
            .expect(
                """src/ProfileViewModel.kt:1: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
class ProfileViewModel : androidx.lifecycle.ViewModel(), kotlinx.coroutines.CoroutineScope
                                                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
            )
    }

    fun `test ViewModel should not have members assignable to CoroutineScope`() {
        val problematicCode = """
            class ProfileViewModel : androidx.lifecycle.ViewModel() {
                private val scope = com.faithlife.CoroutineScopeBase()
            }
        """.trimIndent()

        val result = lint()
            .allowMissingSdk()
            .files(
                java(VIEW_MODEL_JAVA_STUB),
                kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
                kotlin(COROUTINE_SCOPE_KT_STUB),
                kotlin(problematicCode),
            ).run()

        result.expect(
            """src/ProfileViewModel.kt:2: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
    private val scope = com.faithlife.CoroutineScopeBase()
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    fun `test View should not implement CoroutineScope`() {
        val problematicCode = """
            class ProfileView : android.view.View(), kotlinx.coroutines.CoroutineScope
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(kotlin(problematicCode), java(VIEW_JAVA_STUB), kotlin(COROUTINE_SCOPE_KT_STUB))
            .run()
            .expect(
                """src/ProfileView.kt:1: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
class ProfileView : android.view.View(), kotlinx.coroutines.CoroutineScope
                                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
            )
    }

    fun `test View should not have members assignable to CoroutineScope`() {
        val problematicCode = """
            class ProfileView : android.view.View() {
                private val scope = com.faithlife.CoroutineScopeBase()
            }
        """.trimIndent()

        val result = lint()
            .allowMissingSdk()
            .files(
                java(VIEW_JAVA_STUB),
                kotlin(problematicCode),
                kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
                kotlin(COROUTINE_SCOPE_KT_STUB),
            ).run()

        result.expect(
            """src/ProfileView.kt:2: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
    private val scope = com.faithlife.CoroutineScopeBase()
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
        )
    }

    fun `test all fields are checked for CoroutineScope types`() {
        val problematicCode = """
            class ProfileFragment : androidx.lifecycle.LifecycleOwner {
                private val aConstant = 42
                private val scope = com.faithlife.CoroutineScopeBase()
            }
        """.trimIndent()

        lint()
            .allowMissingSdk()
            .files(
                java(LIFECYCLE_JAVA_STUB),
                kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
                kotlin(COROUTINE_SCOPE_KT_STUB),
                kotlin(problematicCode),
            ).run().expect(
                """src/ProfileFragment.kt:3: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
    private val scope = com.faithlife.CoroutineScopeBase()
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""",
            )
    }

    private companion object {
        const val COROUTINE_SCOPE_KT_STUB = """
            package kotlinx.coroutines

            class Job : kotlin.coroutines.CoroutineContext

            enum class Dispatchers : kotlin.coroutines.CoroutineContext {
                MAIN,
                DEFAULT,
            }

            interface CoroutineScope {
                val coroutineContext: kotlin.coroutines.CoroutineContext
            }

            fun CoroutineScope.launch(block: suspend CoroutineScope.() -> Unit) {
                println(this)
            }
        """

        const val COROUTINE_SCOPE_IMPL_KT_STUB = """
            package com.faithlife

            abstract class CoroutineScopeBase : kotlinx.coroutines.CoroutineScope {
                override val coroutineContext: CoroutineContext
                    get() = object : kotlin.coroutines.CoroutineContext {}
            }
        """

        const val LIFECYCLE_JAVA_STUB = """
            package androidx.lifecycle;

            public interface LifecycleOwner {}
        """

        const val FRAGMENT_JAVA_STUB = """
            package androidx.fragment.app;

            public class Fragment implements androidx.lifecycle.LifecycleOwner {}
        """

        const val VIEW_MODEL_JAVA_STUB = """
            package androidx.lifecycle;

            public abstract class ViewModel {}
        """

        const val VIEW_JAVA_STUB = """
            package android.view;

            public abstract class View {}
        """
    }
}
