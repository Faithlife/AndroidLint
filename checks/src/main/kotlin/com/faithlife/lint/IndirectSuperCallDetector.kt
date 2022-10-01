package com.faithlife.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.getContainingUClass

class IndirectSuperCallDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitCallExpression(node: UCallExpression) {
            val method = node.resolve()
            if (node.receiverType == node.getContainingUClass()?.superTypes?.firstOrNull()) {
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
