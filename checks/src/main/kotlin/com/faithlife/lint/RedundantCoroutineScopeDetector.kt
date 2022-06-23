package com.faithlife.lint

import com.android.tools.lint.checks.DataFlowAnalyzer
import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintMap
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.PsiVariable
import com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtSuperTypeList
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UField
import org.jetbrains.uast.UReturnExpression
import org.jetbrains.uast.UastVisibility
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getContainingUFile
import org.jetbrains.uast.getContainingUMethod
import org.jetbrains.uast.getIoFile
import java.util.EnumSet

@Suppress("UnstableApiUsage")
class RedundantCoroutineScopeDetector : Detector(), SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> = listOf("launch", "async")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val methodName = node.methodName ?: return

        // Only consider kotlinx.coroutines.launch and kotlinx.coroutines.async
        if (!context.evaluator.isCoroutineScope(node.receiverType)) return

        val containingClass = checkNotNull(node.getContainingUClass()) {
            "A method call must happen within a class' members."
        }

        // Only consider method calls with a receiver which has an associated coroutine scope
        val providedCoroutineScope = context.evaluator
            .providedCoroutineScope(node.receiverType) ?: return

        // Only consider calls that use the current class as the receiver
        if (node.receiverType != context.evaluator.getClassType(containingClass)) return

        val callLocation = context.getCallLocation(
            node,
            includeReceiver = true,
            includeArguments = false
        )

        Incident()
        context.report(
            issue = ISSUE,
            scope = node as UElement,
            location = callLocation,
            message = MESSAGE,
            fix().replace()
                .sharedName(ISSUE_FIX_FAMILY)
                .range(callLocation)
                .with("$providedCoroutineScope.$methodName")
                .build()
        )
    }

    override fun applicableSuperClasses(): List<String> = listOf(
        "androidx.fragment.app.Fragment",
        "androidx.lifecycle.LifecycleOwner",
        "androidx.lifecycle.ViewModel",
        "android.view.View",
    )

    override fun visitClass(context: JavaContext, declaration: UClass) {
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
                    .build()
            }

            context.report(
                issue = ISSUE,
                scope = entry as PsiElement,
                location = context.getLocation(entry as PsiElement),
                message = MESSAGE,
                fix().composite(
                    fix().replace()
                        .range(location)
                        .with("")
                        .reformat(true)
                        .build(),
                    *coroutineScopeOverridesFixes.toTypedArray()
                )
            )
        }

        // If the class declares fields that implement CoroutineScope or
        // extends a CoroutineScope implementation, suggest alternatives
        val providedCoroutineScope = context.evaluator.providedCoroutineScope(declaration) ?: return
        for (field in declaration.fields.filter { context.evaluator.isCoroutineScope(it.type) }) {
            // The data flow analyzer doesn't work on synthetic methods because they don't have a
            // body in the UAST representation. They need to be checked separately.
            var canFixWithoutBreakingInterface = !field.isVisibleOrHasVisibleAccessor

            // If the field or property is private, check all the methods to
            // see if it is otherwise escaping
            val tracker = object : DataFlowAnalyzer(
                initial = listOf(field),
                initialReferences = listOf(field.javaPsi as PsiVariable)
            ) {
                init {
                    // This is a hack to make the data flow analyzer aware of
                    // the type of the field being tracked (node). The current
                    // implementation assumes that only expressions will ever be
                    // tracked. This is necessary for scope functions like apply
                    // that may have to infer a type for `this`.
                    val type = context.evaluator.getTypeClass(field.type)
                    if (type != null) {
                        types.add(type)
                    }
                }

                override fun field(field: UElement) {
                    // The analyzer doesn't pass information about the assignee.
                    // While it is possible to get information about the lhs from
                    // `field.uastParent`, determining the assigned value's true
                    // visibility might be tricky. For example, if the lhs is a private
                    // mutator method, it is difficult to determine if the underlying field
                    // being assigned by that mutator is public, or if it has a public
                    // accessor. It is possible, but might involve a lot of recursive
                    // method visitation.
                    canFixWithoutBreakingInterface = false
                }

                override fun returns(expression: UReturnExpression) {
                    val method = checkNotNull(expression.getContainingUMethod()) {
                        "Returns must happen from a method"
                    }

                    canFixWithoutBreakingInterface =
                        !LEAKY_VISIBILITY.contains(method.visibility)
                }

                override fun argument(call: UCallExpression, reference: UElement) {
                    val definition = checkNotNull(call.resolve())

                    val cls = checkNotNull(definition.containingClass) {
                        "Only classes can have methods"
                    }

                    // StandardKt functions are common and known to be pure
                    canFixWithoutBreakingInterface = cls == declaration ||
                        cls.qualifiedName == "kotlin.StandardKt__StandardKt"
                }
            }

            for (method in declaration.methods) {
                // If a quick fix would introduce a breaking interface
                // change, don't visit the rest of the methods since we
                // won't add a quickfix to the incident anyway.
                if (!canFixWithoutBreakingInterface) break

                method.accept(tracker)
            }

            val initializer = field.uastInitializer

            context.report(
                issue = ISSUE,
                scope = field as UElement,
                location = context.getLocation(element = field),
                message = MESSAGE,
                if (canFixWithoutBreakingInterface && initializer != null) {
                    fix()
                        .replace()
                        .sharedName(ISSUE_FIX_FAMILY)
                        .reformat(true)
                        .range(context.getLocation(initializer))
                        .with(providedCoroutineScope)
                        .build()
                } else {
                    null
                }
            )
        }
    }

    // Since Kotlin properties (and Java Beans) are comprised of a
    // field and one or two methods. We need to consider the
    // accessor method's visibility also. These are synthetic in Kotlin's
    // case, so the methods don't appear in `declaration.methods`.
    private val UField.isVisibleOrHasVisibleAccessor: Boolean
        get() {
            val getter = getContainingUClass()?.methods?.find { method ->
                method.name == "get${name.capitalizeAsciiOnly()}"
            }

            return LEAKY_VISIBILITY.contains(getter?.visibility ?: visibility)
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

    private fun JavaEvaluator.providedCoroutineScope(type: PsiType?): String? {
        return providedCoroutineScope(getTypeClass(type))
    }

    companion object {
        private const val ISSUE_FIX_FAMILY = "RedundantCoroutineScopeFix"
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
