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
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.kotlin.KotlinUSafeQualifiedExpression

class ForEachFunctionDetector : Detector(), SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> =
        listOf(FOR_EACH_NAME, FOR_EACH_INDEXED_NAME)

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (node.uastParent is KotlinUSafeQualifiedExpression) return

        val receiver = node.receiver
        val receiverTypeClass = context.evaluator.getTypeClass(node.receiverType)

        if (receiver != null && receiverTypeClass != null && context.evaluator.inheritsFrom(receiverTypeClass, "java.lang.Iterable", false)) {
            val lambda = node.valueArguments.first().sourcePsi as KtLambdaExpression
            val receiverName = receiver.asSourceString()

            val replacementText = if (method.name == FOR_EACH_INDEXED_NAME) {
                val firstParameter = lambda.valueParameters[0].text
                val secondParameter = lambda.valueParameters[1].text
                "for (($firstParameter, $secondParameter) in $receiverName.withIndex()) {"
            } else {
                val parameterName = lambda.valueParameters.firstOrNull()?.text ?: "it"
                "for ($parameterName in $receiverName) {"
            }

            val endElement = lambda.functionLiteral.arrow ?: lambda.leftCurlyBrace.psi

            val fix = fix()
                .name("Replace with language-provided for loops", useAsFamilyNameToo = true)
                .replace()
                .range(context.getRangeLocation(receiver.sourcePsi!!, 0, endElement, 0))
                .with(replacementText)
                .build()

            Incident(context)
                .issue(ISSUE)
                .message(MESSAGE)
                .scope(node)
                .location(context.getCallLocation(node, false, false))
                .fix(fix)
                .report()
        }
    }

    companion object {
        const val MESSAGE = "Prefer language-provided for loops."
        val ISSUE = Issue.create(
            id = "ForEachFunctionDetector",
            briefDescription = "Prefer language provided for loops for consistency",
            explanation = """
                $MESSAGE

                `forEach` and `forEachIndexed` often don’t provide a lot of semantic
                benefit over using a Kotlin for loop but add some amount of ambiguity
                to new programmers as to which should be preferred. Typically, collection
                operators do not have side effects but rather transform a collection into
                some other form. `forEach` and `forEachIndexed` demand side effects to be
                useful. The functions also come with strange caveats around loop semantics
                like break and continue.
            """,
            category = Category.PRODUCTIVITY,
            severity = Severity.INFORMATIONAL,
            implementation = Implementation(
                ForEachFunctionDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        private const val FOR_EACH_NAME = "forEach"
        private const val FOR_EACH_INDEXED_NAME = "forEachIndexed"
    }
}
