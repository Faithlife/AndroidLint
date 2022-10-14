package com.faithlife.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.kotlin.KotlinUSafeQualifiedExpression

class ForEachFunctionDetector : Detector(), SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> = listOf("forEach", "forEachIndexed")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (node.uastParent is KotlinUSafeQualifiedExpression) return

        val receiverTypeClass = context.evaluator.getTypeClass(node.receiverType)

        if (receiverTypeClass != null && context.evaluator.inheritsFrom(receiverTypeClass, "java.lang.Iterable", false)) {
            Incident(context)
                .issue(ISSUE)
                .message("Bad") // todo
                .scope(node)
                .location(context.getCallLocation(node, false, false))
                .report()
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "ForEachFunctionDetector",
            briefDescription = "Prefer language provided for loops for consistency",
            explanation = "Prefer language-provided for loops.",
            category = Category.PRODUCTIVITY,
            severity = Severity.INFORMATIONAL,
            implementation = Implementation(
                ForEachFunctionDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
