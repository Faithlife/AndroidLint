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

@Suppress("UnstableApiUsage")
class SimpleDateFormatDetector : Detector(), SourceCodeScanner {

    override fun getApplicableConstructorTypes() = listOf(
        "java.text.SimpleDateFormat",
    )

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
            id = "SimpleDateFormatDetector",
            briefDescription = "Prohibits usage of SimpleDateFormat",
            explanation = "Prefer Java 8 time apis",
            category = Category.CORRECTNESS,
            severity = Severity.WARNING,
            implementation = Implementation(
                SimpleDateFormatDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
