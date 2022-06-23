package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test

@Suppress("UnstableApiUsage")
class RedundantCoroutineScopeDetectorTest : LintDetectorTest() {
    override fun getDetector() = RedundantCoroutineScopeDetector()
    override fun getIssues() = listOf(RedundantCoroutineScopeDetector.ISSUE)

    @Test
    fun `test clean`() {
        val cleanCode = """
            package com.faithlife

            import androidx.fragment.app.Fragment

            class ProfileFragment : Fragment()
        """.trimIndent()

        lint().files(kotlin(cleanCode), java(FRAGMENT_JAVA_STUB), java(LIFECYCLE_JAVA_STUB))
            .run()
            .expectClean()
    }

    @Test
    fun `test classes without associated scopes can implement CoroutineScope`() {
        val cleanCode = """
            package com.faithlife

            class ProfilePresenter : CoroutineScopeBase()
        """.trimIndent()

        lint().files(
            kotlin(cleanCode),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
        )
            .run()
            .expectClean()
    }

    @Test
    fun `test classes without associated scopes can have CoroutineScope fields`() {
        val cleanCode = """
            package com.faithlife

            class ProfilePresenter {
                private val coroutineScope = CoroutineScopeBase()
            }
        """.trimIndent()

        lint().files(
            kotlin(cleanCode),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
        )
            .run()
            .expectClean()
    }

    @Test
    fun `test LifecycleOwner should not implement CoroutineScope`() {
        val problematicCode = """
            package com.faithlife

            import androidx.fragment.app.Fragment
            import kotlinx.coroutines.CoroutineScope

            class ProfileFragment : Fragment(), CoroutineScope
        """.trimIndent()

        val result = lint().files(
            java(FRAGMENT_JAVA_STUB),
            java(LIFECYCLE_JAVA_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(problematicCode),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileFragment.kt:6: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
class ProfileFragment : Fragment(), CoroutineScope
                                    ~~~~~~~~~~~~~~
0 errors, 1 warnings"""
        )
    }

    @Test
    fun `test LifecycleOwner should not extend CoroutineScope implementation`() {
        val problematicCode = """
            package com.faithlife

            import androidx.lifecycle.LifecycleOwner

            class ProfileFragment : CoroutineScopeBase(), LifecycleOwner
        """.trimIndent()

        val result = lint().files(
            java(LIFECYCLE_JAVA_STUB),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(problematicCode),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileFragment.kt:5: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
class ProfileFragment : CoroutineScopeBase(), LifecycleOwner
                        ~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings"""
        )
    }

    @Test
    fun `test LifecycleOwner should not have a member assignable to CoroutineScope`() {
        val problematicCode = """
            package com.faithlife

            import androidx.lifecycle.LifecycleOwner
            import com.faithlife.CoroutineScopeBase

            class ProfileFragment : LifecycleOwner {
                private val scope = CoroutineScopeBase()
            }
        """.trimIndent()

        val result = lint().files(
            java(LIFECYCLE_JAVA_STUB),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(problematicCode),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileFragment.kt:7: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
    private val scope = CoroutineScopeBase()
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings"""
        )
    }

    @Test
    fun `test all fields are checked for CoroutineScope types`() {
        val problematicCode = """
            package com.faithlife

            import androidx.lifecycle.LifecycleOwner
            import com.faithlife.CoroutineScopeBase

            class ProfileFragment : LifecycleOwner {
                private val aConstant = 42
                private val scope = CoroutineScopeBase()
            }
        """.trimIndent()

        val result = lint().files(
            java(LIFECYCLE_JAVA_STUB),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(problematicCode),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileFragment.kt:8: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
    private val scope = CoroutineScopeBase()
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings"""
        )
    }

    @Test
    fun `test ViewModel should not implement CoroutineScope`() {
        val problematicCode = """
            package com.faithlife

            import androidx.lifecycle.ViewModel
            import kotlinx.coroutines.CoroutineScope

            class ProfileViewModel : ViewModel(), CoroutineScope
        """.trimIndent()

        lint().files(
            java(VIEW_MODEL_JAVA_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(problematicCode),
        )
            .run()
            .expect(
                """src/com/faithlife/ProfileViewModel.kt:6: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
class ProfileViewModel : ViewModel(), CoroutineScope
                                      ~~~~~~~~~~~~~~
0 errors, 1 warnings"""
            )
    }

    @Test
    fun `test ViewModel should not have members assignable to CoroutineScope`() {
        val problematicCode = """
            package com.faithlife

            import androidx.lifecycle.ViewModel
            import com.faithlife.CoroutineScopeBase

            class ProfileViewModel : ViewModel() {
                private val scope = CoroutineScopeBase()
            }
        """.trimIndent()

        val result = lint().files(
            java(VIEW_MODEL_JAVA_STUB),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(problematicCode),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileViewModel.kt:7: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
    private val scope = CoroutineScopeBase()
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings"""
        )
    }

    @Test
    fun `test View should not implement CoroutineScope`() {
        val problematicCode = """
            package com.faithlife

            import android.view.View
            import kotlinx.coroutines.CoroutineScope

            class ProfileView : View(), CoroutineScope
        """.trimIndent()

        lint().files(kotlin(problematicCode), java(VIEW_JAVA_STUB), kotlin(COROUTINE_SCOPE_KT_STUB))
            .run()
            .expect(
                """src/com/faithlife/ProfileView.kt:6: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
class ProfileView : View(), CoroutineScope
                            ~~~~~~~~~~~~~~
0 errors, 1 warnings"""
            )
    }

    @Test
    fun `test View should not have members assignable to CoroutineScope`() {
        val problematicCode = """
            package com.faithlife

            import android.view.View
            import com.faithlife.CoroutineScopeBase

            class ProfileView : View() {
                private val scope = CoroutineScopeBase()
            }
        """.trimIndent()

        val result = lint().files(
            java(VIEW_JAVA_STUB),
            kotlin(problematicCode),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileView.kt:7: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
    private val scope = CoroutineScopeBase()
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings"""
        )
    }

    @Test
    fun `test fix class extending a coroutine scope implementation`() {
        val problematicCode = """
            package com.faithlife

            import androidx.lifecycle.LifecycleOwner
            import kotlinx.coroutines.CoroutineContext

            class ProfileFragment : CoroutineScopeBase(), LifecycleOwner {
                override val coroutineContext: CoroutineContext
                    get() = object : CoroutineContext {}
            }
        """.trimIndent()

        val result = lint().files(
            java(LIFECYCLE_JAVA_STUB),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(problematicCode),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileFragment.kt:6: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
class ProfileFragment : CoroutineScopeBase(), LifecycleOwner {
                        ~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings"""
        )

        result.expectFixDiffs(
            """Fix for src/com/faithlife/ProfileFragment.kt line 6: Delete:
@@ -6 +6
- class ProfileFragment : CoroutineScopeBase(), LifecycleOwner {
-     override val coroutineContext: CoroutineContext
-         get() = object : CoroutineContext {}
+ class ProfileFragment : LifecycleOwner {"""
        )
    }

    @Test
    fun `test fix class extending a coroutine scope with multiple overrides`() {
        val problematicCode = """
            package com.faithlife

            import androidx.lifecycle.LifecycleOwner
            import kotlinx.coroutines.CoroutineContext
            import kotlinx.coroutines.CoroutineScope

            interface ALifecycleOwner : CoroutineScope, LifecycleOwner {
                fun lifecycleState(): Int
            }

            class ProfileFragment : ALifecycleOwner {
                override fun lifecycleState(): Int = 0

                override val coroutineContext: CoroutineContext
                    get() = object : CoroutineContext {}
            }
        """.trimIndent()

        val result = lint().files(
            java(LIFECYCLE_JAVA_STUB),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(problematicCode),
        ).run()

        result.expect(
            """src/com/faithlife/ALifecycleOwner.kt:7: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
interface ALifecycleOwner : CoroutineScope, LifecycleOwner {
                            ~~~~~~~~~~~~~~
src/com/faithlife/ALifecycleOwner.kt:11: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
class ProfileFragment : ALifecycleOwner {
                        ~~~~~~~~~~~~~~~
0 errors, 2 warnings"""
        )

        result.expectFixDiffs(
            """Fix for src/com/faithlife/ALifecycleOwner.kt line 7: Delete:
@@ -7 +7
- interface ALifecycleOwner : CoroutineScope, LifecycleOwner {
+ interface ALifecycleOwner : LifecycleOwner {
Fix for src/com/faithlife/ALifecycleOwner.kt line 11: Delete:
@@ -11 +11
- class ProfileFragment : ALifecycleOwner {
+ class ProfileFragment {
@@ -14 +14
-     override val coroutineContext: CoroutineContext
-         get() = object : CoroutineContext {}"""
        )
    }

    @Test
    fun `test fix remove last super type ctor call`() {
        val coroutineScopeBaseWithArgs = """
            package com.faithlife

            import kotlinx.coroutines.CoroutineScope

            class CoroutineScopeBase(val long: Long, val block: () -> Unit) : CoroutineScope
        """.trimIndent()

        val problematicCode = """
            package com.faithlife

            import androidx.lifecycle.LifecycleOwner

            class ProfileFragment : LifecycleOwner, CoroutineScopeBase(-123, {})
        """.trimIndent()

        val result = lint().files(
            java(LIFECYCLE_JAVA_STUB),
            kotlin(coroutineScopeBaseWithArgs),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(problematicCode),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileFragment.kt:5: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
class ProfileFragment : LifecycleOwner, CoroutineScopeBase(-123, {})
                                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings"""
        )

        result.expectFixDiffs(
            """Fix for src/com/faithlife/ProfileFragment.kt line 5: Delete:
@@ -5 +5
- class ProfileFragment : LifecycleOwner, CoroutineScopeBase(-123, {})
@@ -6 +5
+ class ProfileFragment : LifecycleOwner"""
        )
    }

    @Test
    fun `test fix remove first super type ctor call`() {
        val coroutineScopeBaseWithArgs = """
            package com.faithlife

            import kotlinx.coroutines.CoroutineScope

            class CoroutineScopeBase(val long: Long, val block: () -> Unit) : CoroutineScope
        """.trimIndent()

        val problematicCode = """
            package com.faithlife

            import androidx.lifecycle.LifecycleOwner

            class ProfileFragment : CoroutineScopeBase(-123, {}), LifecycleOwner
        """.trimIndent()

        val result = lint().files(
            java(LIFECYCLE_JAVA_STUB),
            kotlin(coroutineScopeBaseWithArgs),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(problematicCode),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileFragment.kt:5: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
class ProfileFragment : CoroutineScopeBase(-123, {}), LifecycleOwner
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings"""
        )

        result.expectFixDiffs(
            """Fix for src/com/faithlife/ProfileFragment.kt line 5: Delete:
@@ -5 +5
- class ProfileFragment : CoroutineScopeBase(-123, {}), LifecycleOwner
@@ -6 +5
+ class ProfileFragment : LifecycleOwner"""
        )
    }

    @Test
    fun `test fix remove middle super type ctor call`() {
        val coroutineScopeBaseWithArgs = """
            package com.faithlife

            import kotlinx.coroutines.CoroutineScope

            class CoroutineScopeBase(val long: Long, val block: () -> Unit) : CoroutineScope
        """.trimIndent()

        val problematicCode = """
            package com.faithlife

            import androidx.lifecycle.LifecycleOwner

            interface AnInterface

            class ProfileFragment : AnInterface, CoroutineScopeBase(-123, {}), LifecycleOwner
        """.trimIndent()

        val result = lint().files(
            java(LIFECYCLE_JAVA_STUB),
            kotlin(coroutineScopeBaseWithArgs),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(problematicCode),
        ).run()

        result.expect(
            """src/com/faithlife/AnInterface.kt:7: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
class ProfileFragment : AnInterface, CoroutineScopeBase(-123, {}), LifecycleOwner
                                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings"""
        )

        result.expectFixDiffs(
            """Fix for src/com/faithlife/AnInterface.kt line 7: Delete:
@@ -7 +7
- class ProfileFragment : AnInterface, CoroutineScopeBase(-123, {}), LifecycleOwner
@@ -8 +7
+ class ProfileFragment : AnInterface, LifecycleOwner"""
        )
    }

    @Test
    fun `test fix class implementing CoroutineScope`() {
        val problematicCode = """
            package com.faithlife

            import androidx.lifecycle.LifecycleOwner
            import kotlinx.coroutines.CoroutineScope

            class ProfileFragment : CoroutineScope, LifecycleOwner
        """.trimIndent()

        val result = lint().files(
            java(LIFECYCLE_JAVA_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(problematicCode),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileFragment.kt:6: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
class ProfileFragment : CoroutineScope, LifecycleOwner
                        ~~~~~~~~~~~~~~
0 errors, 1 warnings"""
        )

        result.expectFixDiffs(
            """Fix for src/com/faithlife/ProfileFragment.kt line 6: Delete:
@@ -6 +6
- class ProfileFragment : CoroutineScope, LifecycleOwner
@@ -7 +6
+ class ProfileFragment : LifecycleOwner"""
        )
    }

    @Test
    fun `test fix coroutine builder with private field receiver`() {
        val problematicCode = """
            package com.faithlife

            import androidx.fragment.app.Fragment
            import com.faithlife.CoroutineScopeBase
            import kotlinx.coroutines.launch

            class ProfileFragment : Fragment() {
                private val scope = CoroutineScopeBase()
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    scope.launch {
                    }
                }
            }
        """.trimIndent()

        val result = lint().files(
            java(FRAGMENT_JAVA_STUB),
            java(LIFECYCLE_JAVA_STUB),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(problematicCode),
        ).run()

        result.expectFixDiffs(
            """Fix for src/com/faithlife/ProfileFragment.kt line 8: Replace with viewLifecycleOwner.lifecycleScope:
@@ -8 +8
-     private val scope = CoroutineScopeBase()
+     private val scope = viewLifecycleOwner.lifecycleScope"""
        )
    }

    @Test
    fun `test fix alias property with androidx-provided coroutine scope`() {
        val problematicCode = """
            package com.faithlife

            import androidx.fragment.app.Fragment
            import kotlinx.coroutines.launch

            class ProfileFragment : Fragment() {
                private val scope = CoroutineScopeBase()
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    scope.apply {
                        launch { }
                    }
                }
            }
        """.trimIndent()

        val result = lint().files(
            java(FRAGMENT_JAVA_STUB),
            java(LIFECYCLE_JAVA_STUB),
            kotlin(problematicCode),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
        ).run()

        result.expectFixDiffs(
            """Fix for src/com/faithlife/ProfileFragment.kt line 7: Replace with viewLifecycleOwner.lifecycleScope:
@@ -7 +7
-     private val scope = CoroutineScopeBase()
+     private val scope = viewLifecycleOwner.lifecycleScope"""
        )
    }

    @Test
    fun `test fix coroutine builder with implicit receiver via class`() {
        val problematicCode = """
            package com.faithlife

            import androidx.fragment.app.Fragment
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.launch

            class ProfileFragment : Fragment(), CoroutineScope {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    launch { }
                }
            }
        """.trimIndent()

        val result = lint().files(
            java(FRAGMENT_JAVA_STUB),
            java(LIFECYCLE_JAVA_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(problematicCode),
        ).run()

        result.expectFixDiffs(
            """Fix for src/com/faithlife/ProfileFragment.kt line 7: Delete:
@@ -7 +7
- class ProfileFragment : Fragment(), CoroutineScope {
+ class ProfileFragment : Fragment() {
Fix for src/com/faithlife/ProfileFragment.kt line 10: Replace with viewLifecycleOwner.lifecycleScope.launch:
@@ -10 +10
-         launch { }
+         viewLifecycleOwner.lifecycleScope.launch { }"""
        )
    }

    @Test
    fun `test fix coroutine builder with implicit receiver via with`() {
        val problematicCode = """
            package com.faithlife

            import androidx.fragment.app.Fragment
            import kotlinx.coroutines.launch

            class ProfileFragment : Fragment() {
                private val scope = CoroutineScopeBase()
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    with(scope) {
                        launch { }
                    }
                }
            }
        """.trimIndent()

        val result = lint().files(
            java(FRAGMENT_JAVA_STUB),
            java(LIFECYCLE_JAVA_STUB),
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            kotlin(problematicCode),
        ).run()

        result.expectFixDiffs(
            """Fix for src/com/faithlife/ProfileFragment.kt line 7: Replace with viewLifecycleOwner.lifecycleScope:
@@ -7 +7
-     private val scope = CoroutineScopeBase()
+     private val scope = viewLifecycleOwner.lifecycleScope"""
        )
    }

    @Test
    fun `test public CoroutineScope field is not changed`() {
        val problematicCodeWithoutEasyFix = """
            package com.faithlife

            import androidx.fragment.app.Fragment
            import kotlinx.coroutines.launch

            class ProfileFragment : Fragment() {
                @JvmField val scope = CoroutineScopeBase()
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    scope.apply {
                        launch { }
                    }
                }
            }
        """.trimIndent()

        val result = lint().files(
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            java(LIFECYCLE_JAVA_STUB),
            java(FRAGMENT_JAVA_STUB),
            kotlin(problematicCodeWithoutEasyFix),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileFragment.kt:7: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
    @JvmField val scope = CoroutineScopeBase()
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings"""
        )
        result.expectFixDiffs("")
    }

    @Test
    fun `test public CoroutineScope property is not changed`() {
        // A property has synthetic accessors, which are represented
        // distinct from UFields in UAST.
        val problematicCodeWithoutEasyFix = """
            package com.faithlife

            import androidx.fragment.app.Fragment
            import kotlinx.coroutines.launch

            class ProfileFragment : Fragment() {
                val scope = CoroutineScopeBase()
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    scope.apply {
                        launch { }
                    }
                }
            }
        """.trimIndent()

        val result = lint().files(
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            java(LIFECYCLE_JAVA_STUB),
            java(FRAGMENT_JAVA_STUB),
            kotlin(problematicCodeWithoutEasyFix),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileFragment.kt:7: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
    val scope = CoroutineScopeBase()
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings"""
        )
        result.expectFixDiffs("")
    }

    @Test
    fun `test CoroutineScope property returned from public function is not changed`() {
        // A property has synthetic accessors, which are represented
        // distinct from UFields in UAST.
        val problematicCodeWithoutEasyFix = """
            package com.faithlife

            import androidx.fragment.app.Fragment
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.launch

            class ProfileFragment : Fragment() {
                private val scope = CoroutineScopeBase()

                fun pleaseDoNotDoThis(): CoroutineScope {
                    val itTracksAssignment = scope
                    return itTracksAssignment
                }
            }
        """.trimIndent()

        val result = lint().files(
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            java(LIFECYCLE_JAVA_STUB),
            java(FRAGMENT_JAVA_STUB),
            kotlin(problematicCodeWithoutEasyFix),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileFragment.kt:8: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
    private val scope = CoroutineScopeBase()
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings"""
        )
        result.expectFixDiffs("")
    }

    @Test
    fun `test private CoroutineScope property stored in field is not changed`() {
        // A property has synthetic accessors, which are represented
        // distinct from UFields in UAST.
        val problematicCodeWithoutEasyFix = """
            package com.faithlife

            import androidx.fragment.app.Fragment
            import kotlinx.coroutines.CoroutineScope

            class ProfileFragment : Fragment() {
                private val scope = CoroutineScopeBase()
                lateinit var sneakyScope: CoroutineScope

                fun sneakingExfiltration() {
                    sneakyScope = scope
                }
            }
        """.trimIndent()

        val result = lint().files(
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            java(LIFECYCLE_JAVA_STUB),
            java(FRAGMENT_JAVA_STUB),
            kotlin(problematicCodeWithoutEasyFix),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileFragment.kt:7: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
    private val scope = CoroutineScopeBase()
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
src/com/faithlife/ProfileFragment.kt:8: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
    lateinit var sneakyScope: CoroutineScope
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 2 warnings"""
        )
        result.expectFixDiffs("")
    }

    @Test
    fun `test private CoroutineScope property stored in convoluted field is not changed`() {
        // A property has synthetic accessors, which are represented
        // distinct from UFields in UAST.
        val problematicCodeWithoutEasyFix = """
            package com.faithlife

            import androidx.fragment.app.Fragment
            import kotlinx.coroutines.CoroutineScope

            class ProfileFragment : Fragment() {
                private val scope = CoroutineScopeBase()
                lateinit var sneakyScope: CoroutineScope

                fun superSneakyExfiltration() {
                    scope.let {
                        it.apply {
                            sneakyScope = this
                        }
                    }
                }
            }
        """.trimIndent()

        val result = lint().files(
            kotlin(COROUTINE_SCOPE_KT_STUB),
            kotlin(COROUTINE_SCOPE_IMPL_KT_STUB),
            java(LIFECYCLE_JAVA_STUB),
            java(FRAGMENT_JAVA_STUB),
            kotlin(problematicCodeWithoutEasyFix),
        ).run()

        result.expect(
            """src/com/faithlife/ProfileFragment.kt:7: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
    private val scope = CoroutineScopeBase()
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
src/com/faithlife/ProfileFragment.kt:8: Warning: Consider scopes provided by the class. [RedundantCoroutineScopeDetector]
    lateinit var sneakyScope: CoroutineScope
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 2 warnings"""
        )
        result.expectFixDiffs("")
    }

    private companion object {
        const val COROUTINE_SCOPE_KT_STUB = """
            package kotlinx.coroutines

            interface CoroutineContext

            interface CoroutineScope {
                val coroutineContext: CoroutineContext
            }

            fun CoroutineScope.launch(block: suspend CoroutineScope.() -> Unit) {
                println(this)
            }
        """

        const val COROUTINE_SCOPE_IMPL_KT_STUB = """
            package com.faithlife

            import kotlinx.coroutines.CoroutineContext
            import kotlinx.coroutines.CoroutineScope

            abstract class CoroutineScopeBase : CoroutineScope {
                override val coroutineContext: CoroutineContext
                    get() = object : CoroutineContext {}
            }
        """

        const val LIFECYCLE_JAVA_STUB = """
            package androidx.lifecycle;

            public interface LifecycleOwner {}
        """

        const val FRAGMENT_JAVA_STUB = """
            package androidx.fragment.app;

            import androidx.lifecycle.LifecycleOwner;

            public class Fragment implements LifecycleOwner {}
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
