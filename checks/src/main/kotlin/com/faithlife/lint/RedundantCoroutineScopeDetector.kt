package com.faithlife.lint

import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtSuperTypeList
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UastVisibility
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getContainingUFile
import org.jetbrains.uast.getIoFile
import org.jetbrains.uast.toUElement
import org.jetbrains.uast.visitor.AbstractUastVisitor
import java.util.EnumSet

@Suppress("UnstableApiUsage")
class RedundantCoroutineScopeDetector : Detector(), SourceCodeScanner {
    override fun applicableSuperClasses(): List<String> = listOf(
        "androidx.fragment.app.Fragment",
        "androidx.lifecycle.LifecycleOwner",
        "androidx.lifecycle.ViewModel",
        "android.view.View",
    )

    override fun visitClass(context: JavaContext, declaration: UClass) {
        val newScope = checkNotNull(context.evaluator.providedCoroutineScope(declaration)) {
            "Classes defined in applicableSuperClasses should always have a replacement"
        }

        // If the class implements CoroutineScope or extends a CoroutineScope
        // implementation, recommend using newScope instead
        detectSuperType(context, declaration, newScope)

        // If the class declares fields that implement CoroutineScope or
        // extends a CoroutineScope implementation, recommend using newScope
        detectPrivateMembers(context, declaration, newScope)
    }

    private fun detectPrivateMembers(
        context: JavaContext,
        declaration: UClass,
        providedCoroutineScope: String,
    ) {
        for (field in declaration.fields.filter { context.evaluator.isCoroutineScope(it.type) }) {
            val coroutineScopeCallSiteFixes = mutableListOf<LintFix>()

            val referenceExpressionVisitor = object : AbstractUastVisitor() {
                override fun visitSimpleNameReferenceExpression(
                    node: USimpleNameReferenceExpression
                ): Boolean {
                    // If a field is referenced, add a fix regardless if the implementation
                    // uses a property accessor (typically implicit for public properties) or
                    // uses the field directly (typical of private properties)
                    if (node.resolve().toUElement()?.sourcePsi == field.sourcePsi) {
                        coroutineScopeCallSiteFixes.add(
                            fix().replace()
                                .range(context.getLocation(node))
                                .with(providedCoroutineScope)
                                .reformat(true)
                                .build()
                        )

                        return true
                    }

                    return super.visitSimpleNameReferenceExpression(node)
                }
            }

            val methodsWithAccessToField = declaration.methods +
                declaration.innerClasses.flatMap { it.methods.asIterable() }

            for (method in methodsWithAccessToField) {
                method.accept(referenceExpressionVisitor)
            }

            Incident(context)
                .issue(ISSUE)
                .at(field)
                .message(MESSAGE)
                .fix(
                    if (field.uastInitializer != null) {
                        fix().name("Delete CoroutineScope member").composite(
                            fix()
                                .replace()
                                .range(context.getLocation(field))
                                .with("")
                                .reformat(true)
                                .build(),
                            *coroutineScopeCallSiteFixes.toTypedArray()
                        )
                    } else {
                        null
                    }
                ).report()
        }
    }

    private fun detectSuperType(
        context: JavaContext,
        declaration: UClass,
        providedCoroutineScope: String,
    ) {
        // If the class inherits CoroutineScope, suggest alternatives.
        val info = context.determineCoroutineScopeSuperTypePosition(declaration)
        if (info != null) {
            val (location, entry) = info

            val coroutineScopeClass = context.evaluator
                .findClass("kotlinx.coroutines.CoroutineScope")

            // Cleanup all the overrides
            val coroutineScopeOverrides = declaration.methods
                .filter { it.findSuperMethods(coroutineScopeClass).isNotEmpty() }

            val coroutineScopeOverridesFixes = coroutineScopeOverrides.map { method ->
                val methodPsi = method.sourcePsi
                val element = if (methodPsi is KtPropertyAccessor) {
                    // Delete the whole property, not just the accessor
                    methodPsi.getParentOfType<KtProperty>(false)
                } else {
                    method
                } as PsiElement

                fix().replace()
                    .range(context.getLocation(element))
                    .with("")
                    .reformat(true)
                    .build()
            }

            // Cleanup the field references
            val coroutineScopeCallSiteFixes = mutableListOf<LintFix>()
            val callVisitor = object : AbstractUastVisitor() {
                override fun visitCallExpression(node: UCallExpression): Boolean {
                    val methodName = node.methodName ?: return false

                    if (methodName !in setOf("async", "launch")) {
                        return false
                    }

                    // Only consider kotlinx.coroutines.launch and kotlinx.coroutines.async
                    if (!context.evaluator.isCoroutineScope(node.receiverType)) {
                        return false
                    }

                    val containingClass = checkNotNull(node.getContainingUClass()) {
                        "A method call must happen within a class' members."
                    }

                    // Only consider calls that use the current class as the receiver
                    if (node.receiverType != context.evaluator.getClassType(declaration)) {
                        return false
                    }

                    val callLocation = context.getCallLocation(
                        node,
                        includeReceiver = true,
                        includeArguments = false
                    )

                    coroutineScopeCallSiteFixes.add(
                        fix().replace()
                            .range(callLocation)
                            .with("$providedCoroutineScope.$methodName")
                            .reformat(true)
                            .build()
                    )

                    return true
                }
            }

            val methodsWithAccessToField = declaration.methods +
                declaration.innerClasses.flatMap { it.methods.asIterable() }

            for (method in methodsWithAccessToField) {
                method.accept(callVisitor)
            }

            Incident(context)
                .issue(ISSUE)
                .at(entry)
                .message(MESSAGE)
                .fix(
                    fix().name("Delete CoroutineScope supertype").composite(
                        fix().replace()
                            .range(location)
                            .with("")
                            .reformat(true)
                            .build(),
                        *(coroutineScopeOverridesFixes + coroutineScopeCallSiteFixes).toTypedArray()
                    )
                ).report()
        }
    }

    private fun JavaEvaluator.isCoroutineScope(type: PsiType?): Boolean {
        val cls = getTypeClass(type) ?: return false
        return implementsInterface(cls, "kotlinx.coroutines.CoroutineScope")
    }

    private fun JavaEvaluator.providedCoroutineScope(psiClass: PsiClass?): String? {
        if (psiClass == null) return null

        return when {
            extendsClass(
                psiClass,
                "androidx.lifecycle.ViewModel",
            ) -> "viewModelScope"
            extendsClass(
                psiClass,
                "androidx.fragment.app.Fragment",
            ) -> "viewLifecycleOwner.lifecycleScope"
            implementsInterface(
                psiClass,
                "androidx.lifecycle.LifecycleOwner",
            ) -> "viewLifecycleOwner"
            extendsClass(
                psiClass,
                "android.view.View",
            ) -> "findViewTreeLifecycleOwner()?.lifecycleScope"
            else -> null
        }
    }

    private fun JavaContext.determineCoroutineScopeSuperTypePosition(
        declaration: UClass,
    ): Pair<Location, KtSuperTypeListEntry>? {
        val file = declaration.getContainingUFile()?.getIoFile() ?: return null
        val ktClass = declaration.sourcePsi as? KtClass ?: return null

        val coroutineScopeSuperType = declaration.uastSuperTypes
            .find { evaluator.isCoroutineScope(it.type) } ?: return null

        val superTypeList = ktClass.findDescendantOfType<KtSuperTypeList>()
        val superTypeEntry = coroutineScopeSuperType.sourcePsi
            ?.getParentOfType<KtSuperTypeListEntry>(false)!!

        return when {
            // The whole type list needs to be deleted.
            declaration.uastSuperTypes.size == 1 -> {
                val colon = ktClass.getColon()

                val superTypeListLocation = getLocation(superTypeList)
                val colonLocation = getLocation(colon)
                val whiteSpaceLocation = (colon?.prevSibling as? PsiWhiteSpace)
                    ?.let(this::getLocation)

                Location.create(
                    file,
                    (whiteSpaceLocation?.start ?: colonLocation.start!!),
                    superTypeListLocation.end,
                )
            }
            // Take care of the preceding comma since trailing commas in
            // supertype lists are illegal syntax.
            declaration.uastSuperTypes.size > 1 &&
                coroutineScopeSuperType == declaration.uastSuperTypes.last() -> {
                var previousEntry = superTypeEntry.prevSibling
                while (previousEntry.text != ",") {
                    previousEntry = previousEntry.prevSibling
                }

                if (previousEntry.prevSibling is PsiWhiteSpace) {
                    previousEntry = previousEntry.prevSibling
                }

                val start = getLocation(previousEntry)
                Location.create(file, start.start!!, getLocation(superTypeEntry).end)
            }
            // In all other cases, delete the trailing comma, which also handles an
            // illegal syntax situation if the first type is deleted.
            else -> {
                var nextEntry = superTypeEntry.nextSibling
                while (nextEntry.text != ",") {
                    nextEntry = nextEntry.nextSibling
                }

                if (nextEntry.nextSibling is PsiWhiteSpace) {
                    nextEntry = nextEntry.nextSibling
                }

                val end = getLocation(nextEntry)
                Location.create(file, getLocation(superTypeEntry).start!!, end.end)
            }
        } to superTypeEntry
    }

    private val UastVisibility.isLeaky: Boolean
        get() = LEAKY_VISIBILITY.contains(this)

    companion object {
        private const val MESSAGE = "Consider scopes provided by the class."

        private val LEAKY_VISIBILITY = EnumSet.of(
            UastVisibility.PROTECTED,
            UastVisibility.PACKAGE_LOCAL,
            UastVisibility.PUBLIC,
        )

        val ISSUE = Issue.create(
            "RedundantCoroutineScopeDetector",
            "Redundant CoroutineScope",
            """
                $MESSAGE
                For LifecycleOwner: `lifecycleScope`
                For ViewModel: `viewModelScope`
                For Fragment: Prefer `viewLifecycleOwner.lifecycleScope` over `lifecycleScope`
                For Views: `findViewTreeLifecycleOwner()?.lifecycleScope`
            """,
            moreInfo = "https://developer.android.com/topic/libraries/architecture/coroutines",
            category = Category.CORRECTNESS,
            severity = Severity.WARNING,
            implementation = Implementation(
                RedundantCoroutineScopeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            ),
            androidSpecific = true
        )
    }
}
