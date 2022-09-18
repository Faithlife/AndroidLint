package com.faithlife.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

class ObsoleteDateInstantiationDetector : Detector(), SourceCodeScanner {

    override fun getApplicableConstructorTypes() = listOf("java.util.Date")

    override fun visitConstructor(
        context: JavaContext,
        node: UCallExpression,
        constructor: PsiMethod
    ) {
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = "Use Java 8 time APIs instead"
        )
    }

    companion object {
        val ISSUE = Issue.create(
            id = "ObsoleteDateInstantiationDetector",
            briefDescription = "Prohibits the use of java.util.Date",
            explanation = "Prefer Java 8 time APIs.",
            category = Category.CORRECTNESS,
            severity = Severity.WARNING,
            implementation = Implementation(
                ObsoleteDateInstantiationDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
