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
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.kotlin.KotlinUSwitchEntry
import org.jetbrains.uast.kotlin.KotlinUSwitchExpression

@Suppress("UnstableApiUsage")
class FiniteWhenCasesDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes() = listOf(UExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitExpression(node: UExpression) {
            val whenExpression = node as? KotlinUSwitchExpression ?: return

            val isSealed = context.evaluator.isSealed(context.evaluator.getTypeClass(whenExpression.expression!!.getExpressionType()))
            val isEnum = context.evaluator.extendsClass(context.evaluator.getTypeClass(whenExpression.expression!!.getExpressionType()), "java.lang.Enum", true)

            val elseBranch = node.body.expressions
                .filterIsInstance<KotlinUSwitchEntry>()
                .find { it.caseValues.isEmpty() } // else -> is the only possible case with empty values

            if ((isEnum || isSealed) && elseBranch != null) {
                val incident = Incident(context)
                    .issue(ISSUE)
                    .message(MESSAGE)
                    .scope(node)
                    .location(context.getLocation(elseBranch))

                incident.report()
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

                This should not be used if the when subject might possibly introduce
                new cases without this module being recompiled. One common way this can
                happen is if the application consumes jars delivered out of sync with the app.

                This usually doesn't happen with android applications, which typically
                consume all libraries being used (other than the Android Framework)
                at compile time.
            """,
            moreInfo = "https://kotlinlang.org/docs/control-flow.html#when-expression",
            category = Category.CORRECTNESS,
            severity = Severity.WARNING,
            implementation = Implementation(
                FiniteWhenCasesDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            ),
            androidSpecific = false,
            enabledByDefault = false,
        )
    }
}
