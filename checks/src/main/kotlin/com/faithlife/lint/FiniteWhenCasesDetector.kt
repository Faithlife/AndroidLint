package com.faithlife.lint

import com.android.resources.Navigation
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
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.types.KotlinType
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
                        val whenExpression = expression as? KotlinUSwitchExpression ?: continue

                        val isSealed = context.evaluator.isSealed(context.evaluator.getTypeClass(whenExpression.expression!!.getExpressionType()))
                        val isEnum = context.evaluator.extendsClass(context.evaluator.getTypeClass(whenExpression.expression!!.getExpressionType()), "java.lang.Enum", true)

                        val elseBranch = expression.body.expressions.find {
                            it.sourcePsi?.text?.trim()?.startsWith("else") == true
                        }

                        if ((isEnum || isSealed) && elseBranch != null) {
                            val incident = Incident(context)
                                .issue(ISSUE)
                                .message(MESSAGE)
                                .scope(expression)
                                .location(context.getLocation(elseBranch))

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
