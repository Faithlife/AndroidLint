package com.faithlife.lint

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

class ObsoleteDateInstantiationDetector : Detector(), SourceCodeScanner {

    override fun getApplicableConstructorTypes(): List<String>? {
        return listOf("java.util.Date")
    }

    override fun visitConstructor(context: JavaContext, node: UCallExpression, constructor: PsiMethod) {
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = "Use Java 8 time APIs instead."
        )
    }

    companion object {
        val ISSUE = Issue.create(
            id = "ObsoleteDateInstantiationDetector",
            briefDescription = "Prohibits the use of java.util.Date",
            explanation = "Prefer Java 8 time APIs.",
            category = Category.CORRECTNESS,
            severity = Severity.ERROR,
            implementation = Implementation(
                ObsoleteDateInstantiationDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
