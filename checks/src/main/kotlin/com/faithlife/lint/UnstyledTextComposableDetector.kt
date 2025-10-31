package com.faithlife.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMember
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiVariable
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.UastFacade

class UnstyledTextComposableDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("Text")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val packageName = method.containingClass?.qualifiedName?.substringBeforeLast(".")
        if (packageName != "androidx.compose.material3") return

        val styleArgument = node.valueArguments.find {
            it.getExpressionType()?.canonicalText?.startsWith("androidx.compose.ui.text.TextStyle") == true
        }

        if (styleArgument == null || styleArgument.resolvesToNull()) {
            context.report(
                ISSUE_TEXT_COMPOSABLE_NO_STYLE,
                node,
                context.getLocation(node),
                "Text() composable is missing a style parameter",
            )
        } else {
            val resolved = (styleArgument as? UReferenceExpression)?.resolve() ?: return

            val fqName = when (resolved) {
                is KtProperty -> resolved.containingClassOrObject?.fqName?.asString()
                is PsiMember -> resolved.containingClass?.qualifiedName
                else -> null
            }

            if (fqName?.contains("androidx.compose.material3.MaterialTheme.typography") == true) {
                context.report(
                    ISSUE_TEXT_COMPOSABLE_MATERIAL_TYPOGRAPHY,
                    styleArgument,
                    context.getLocation(styleArgument),
                    "Do not use MaterialTheme.typography for Text styles.",
                )
            }
        }
    }

    private fun UExpression.resolvesToNull(): Boolean = when (this) {
        is ULiteralExpression -> isNull
        is UReferenceExpression -> {
            when (val resolved = resolve()) {
                is KtProperty -> resolved.initializer?.text == "null"
                is PsiVariable -> UastFacade.getInitializerBody(resolved)?.resolvesToNull() == true
                else -> false
            }
        }
        else -> false
    }

    companion object {
        val ISSUE_TEXT_COMPOSABLE_NO_STYLE = Issue.create(
            id = "UnstyledTextComposable",
            briefDescription = "Text composable should always have a style.",
            explanation = """
            All Text() composables should specify a style parameter to ensure:
            * The style matches the spec
            * No values were overlooked
            * Consistency across different UI
            * Ease of digging into the style

            While it is preferred to only use a style that already exists, properties can be overridden:
            Text(
                text = stringResource(R.string.hello),
                style = myTextStyle,
                color = Color.Black,
            )
            """,
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                UnstyledTextComposableDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        val ISSUE_TEXT_COMPOSABLE_MATERIAL_TYPOGRAPHY = Issue.create(
            id = "Material3Typography",
            briefDescription = "Use a Logos semantic text style instead of MaterialTheme.typography.",
            explanation = """
            Our design system doesn't follow material theming so it is confusing to try to conform
            to the text style defined in MaterialTheme.typography. Additionally, it is not obvious
            how to discover the actual values that the MaterialTheme.typography styles define.

            Example of incorrect usage:
            Text(
                text = "Hello",
                style = MaterialTheme.typography.bodyMedium
            )

            Example of correct usage:
            Text(
                text = "Hello",
                style = textStyles.genericText
            )
            """,
            category = Category.PRODUCTIVITY,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                UnstyledTextComposableDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
