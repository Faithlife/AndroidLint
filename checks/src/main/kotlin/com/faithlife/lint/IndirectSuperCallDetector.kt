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

class IndirectSuperCallDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitCallExpression(node: UCallExpression) {
            val containingMethod = node.getContainingUMethod() ?: return

            if (context.evaluator.getSuperMethod(containingMethod) != node.resolve() && node.receiverType in (node.getContainingUClass()?.superTypes ?: emptyArray())) {
                context.report(ISSUE, context.getCallLocation(node, includeArguments = false, includeReceiver = true), "")
            }
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "IndirectSuperCallDirector",
            briefDescription = "Only call super methods from overrides",
            explanation = "TODO",
            category = Category.CORRECTNESS,
            severity = Severity.WARNING,
            implementation = Implementation(
                IndirectSuperCallDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
