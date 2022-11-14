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
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiType
import org.jetbrains.uast.UCatchClause

class ErrorCatchDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes() = listOf(UCatchClause::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitCatchClause(node: UCatchClause) {
            @Suppress("UNCHECKED_CAST")
            val typeClassesToTypes = node.types.associateBy(context.evaluator::getTypeClass)
                .filterKeys { it != null } as Map<PsiClass, PsiType>

            val isTooGeneric = typeClassesToTypes.keys.firstOrNull { typeClass ->
                typeClass.qualifiedName == "java.lang.Throwable"
            }

            if (isTooGeneric != null) {
                val typeInCatchList = typeClassesToTypes[isTooGeneric]
                Incident(context)
                    .issue(createIssue(TOO_GENERIC_MESSAGE))
                    .message(TOO_GENERIC_MESSAGE)
                    .scope(node)
                    .location(context.getLocation(typeInCatchList))
                    .report()
                return
            }

            val isError = typeClassesToTypes.keys.firstOrNull { typeClass ->
                context.evaluator.extendsClass(typeClass, "java.lang.Error")
            }

            if (isError != null) {
                val typeInCatchList = typeClassesToTypes[isError]
                Incident(context)
                    .issue(createIssue(ERROR_CAUGHT_MESSAGE))
                    .message(ERROR_CAUGHT_MESSAGE)
                    .scope(node)
                    .location(context.getLocation(typeInCatchList))
                    .report()
                return
            }
        }
    }

    companion object {
        private const val TOO_GENERIC_MESSAGE = "Catching Throwable will include Errors. Be more specific."
        private const val ERROR_CAUGHT_MESSAGE = "Errors should not be caught."

        private fun createIssue(message: String): Issue {
            return Issue.create(
                "ErrorCatchDetector",
                "A catch expression can catch an Error.",
                """
                $message

                Catching errors can further complicate
            """,
                moreInfo = "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Error.html",
                category = Category.CORRECTNESS,
                severity = Severity.ERROR,
                implementation = Implementation(
                    ErrorCatchDetector::class.java,
                    Scope.JAVA_FILE_SCOPE,
                ),
            )
        }
    }
}
