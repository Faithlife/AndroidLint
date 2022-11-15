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
            val typeClassesToTypes = node.types
                .associateBy(context.evaluator::getTypeClass)
                .filterKeys { it != null } as Map<PsiClass, PsiType>

            val isTooGeneric = typeClassesToTypes.keys.firstOrNull { typeClass ->
                typeClass.qualifiedName == "java.lang.Throwable"
            }

            if (isTooGeneric != null) {
                val typeInCatchList = typeClassesToTypes[isTooGeneric]
                Incident(context)
                    .issue(ISSUE_CATCH_TOO_GENERIC)
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
                    .issue(ISSUE_ERROR_CAUGHT)
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
        private const val DESC = "Catch blocks should not handle java.lang.Error"
        private fun createExplanation(message: String) = """
            $message

            Catching errors can further complicate stacktraces and error investigation
            in general. `java.lang.Error` and subtypes should not be caught. They indicate
            a terminal program state that is best served by crashing quickly in order to provide
            the best view of application state that lead to the `Error` being thrown.
        """

        val ISSUE_CATCH_TOO_GENERIC = Issue.create(
            "ThrowableCatchDetector",
            DESC,
            createExplanation(TOO_GENERIC_MESSAGE),
            moreInfo = "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Error.html",
            category = Category.CORRECTNESS,
            severity = Severity.ERROR,
            implementation = Implementation(
                ErrorCatchDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        val ISSUE_ERROR_CAUGHT = Issue.create(
            "ErrorCatchDetector",
            DESC,
            createExplanation(ERROR_CAUGHT_MESSAGE),
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
