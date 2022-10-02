package com.faithlife.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.getContainingUClass
import org.jetbrains.uast.getContainingUMethod

class SkippedClassLocalOverrideDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitCallExpression(node: UCallExpression) {
            val method = checkNotNull(node.resolve()) { "A call expression must resolve to a method." }

            val hasClassLocalOverride = node.getContainingUClass()?.methods
                ?.mapNotNull(context.evaluator::getSuperMethod)
                ?.any { method == it } ?: false

            if (!hasClassLocalOverride) return

            val superMethod = context.evaluator.getSuperMethod(node.getContainingUMethod())

            if (superMethod == null || superMethod != method) {
                context.report(
                    ISSUE,
                    context.getCallLocation(node, includeArguments = false, includeReceiver = true),
                    MESSAGE,
                )
            }
        }
    }

    companion object {
        val MESSAGE = "Explicit super calls should only appear in corresponding overrides."
        val ISSUE = Issue.create(
            id = "IndirectSuperCallDirector",
            briefDescription = "Only call super methods from overrides",
            explanation = """
                Calling super methods that have side effects (like many Android lifecycle methods)
                multiple times can lead to hard to track down bugs. Usually, if there's an override
                for a method in a class, you want to call the super method from it to avoid that
                risk.
            """,
            category = Category.CORRECTNESS,
            severity = Severity.WARNING,
            implementation = Implementation(
                SkippedClassLocalOverrideDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
