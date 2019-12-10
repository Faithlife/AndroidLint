package com.faithlife.lint

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

class SimpleDateFormatDetector : Detector(), SourceCodeScanner {

    override fun getApplicableConstructorTypes(): List<String>? {
        return listOf("java.text.SimpleDateFormat")
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
            id = "SimpleDateFormatDetector",
            briefDescription = "Prohibits usage of SimpleDateFormat",
            explanation = "Prefer Java 8 time apis",
            category = Category.CORRECTNESS,
            severity = Severity.ERROR,
            implementation = Implementation(
                SimpleDateFormatDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
