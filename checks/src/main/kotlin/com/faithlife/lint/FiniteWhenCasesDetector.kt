package com.faithlife.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.kotlin.KotlinUSwitchExpression

@Suppress("UnstableApiUsage")
class FiniteWhenCasesDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes() = listOf(UMethod::class.java)
    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitMethod(node: UMethod) {
            when (val body = node.uastBody) {
                is UBlockExpression -> {
                    for (expression in body.expressions) {
                        // todo: check subject. This fails on every use of else in when, which is wrong
                        if (expression is KotlinUSwitchExpression &&
                            expression.body.expressions.any { it.sourcePsi?.text?.startsWith("else") == true }) {

                            val incident = Incident(context)
                                .issue(ISSUE)
                                .message(MESSAGE)
                                .scope(expression)
                                .location(context.getLocation(expression))

                            incident.report()
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val MESSAGE = "Prefer explicit case handling over else."

        val ISSUE = Issue.create(
            "FiniteWhenCasesDetector",
            "Avoid else where possible",
            """
                $MESSAGE

                Explicit handling of when cases encourages reconsideration of
                relevant when expressions if the subject of the expression changes.
            """,
            moreInfo = "https://kotlinlang.org/docs/control-flow.html#when-expression",
            category = Category.CORRECTNESS,
            severity = Severity.WARNING,
            implementation = Implementation(
                FiniteWhenCasesDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            ),
            androidSpecific = false
        )
    }
}
