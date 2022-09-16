package com.faithlife.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintMap
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.kotlin.KotlinUSwitchEntry
import org.jetbrains.uast.kotlin.KotlinUSwitchExpression

@Suppress("UnstableApiUsage")
class FiniteWhenCasesDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes() = listOf(UExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitExpression(node: UExpression) {
            val whenExpression = node as? KotlinUSwitchExpression ?: return
            val subjectType = whenExpression.expression?.getExpressionType() ?: return
            val subjectTypeClass = context.evaluator.getTypeClass(subjectType) ?: return

            val finiteCases = context.evaluator.isSealed(subjectTypeClass) ||
                context.evaluator.extendsClass(subjectTypeClass, "java.lang.Enum", true)

            val elseBranch = node.body.expressions
                .filterIsInstance<KotlinUSwitchEntry>()
                .find { it.sourcePsi.isElse }

            if (finiteCases && elseBranch != null) {
                val incident = Incident(context)
                    .issue(ISSUE)
                    .message(MESSAGE)
                    .scope(node)
                    .location(context.getLocation(elseBranch))

                context.report(
                    incident,
                    map().put(
                        KEY_IS_SUBJECT_TYPE_PUBLIC,
                        context.evaluator.isPublic(subjectTypeClass)
                    ).put(
                        KEY_IS_FRAMEWORK_TYPE,
                        subjectTypeClass.isAndroidFrameworkType
                    )
                )
            }
        }
    }

    override fun filterIncident(context: Context, incident: Incident, map: LintMap): Boolean {
        // Since the android framework might add new cases to a when subject
        // during framework upgrades without recompiling our app from source, enforcing
        // this rule on subjects of that category would make things brittle.
        if (map.getBoolean(KEY_IS_FRAMEWORK_TYPE, false)!!) return false

        // If the main project isn't a library, it's an app.
        // Apps always recompile against direct dependencies, so binary
        // incompatibility is less of a risk.
        //
        // Directing Lint to check app project source dependencies
        // via lint.checkDependencies gives the tool more information
        // around which dependencies are always recompiled with the app project.
        if (!context.mainProject.isLibrary) return true

        // There's no way (without reflection) for a non-public type
        // to introduce binary incompatibilities, so allow this rule to apply
        // to the code where the subject type is accessible.
        return !map.getBoolean(KEY_IS_SUBJECT_TYPE_PUBLIC, true)!!
    }

    companion object {
        private const val KEY_IS_FRAMEWORK_TYPE = "IsFrameworkType"
        private const val KEY_IS_SUBJECT_TYPE_PUBLIC = "IsPublic"
        private const val MESSAGE = "Prefer explicit case handling over else."

        val ISSUE = Issue.create(
            "FiniteWhenCasesDetector",
            "Avoid else where possible",
            """
                $MESSAGE

                Explicit handling of when cases encourages reconsideration of
                relevant when expressions if the subject of the expression changes.

                This check should only run in modules that will either be compiled:
                    a. with an app and do not produce artifacts that are used elsewhere.
                    b. as a stand alone library artifact

                Scenario b will run the check in a less-sensitive mode that should
                avoid encouraging brittle libraries. Only non-public when subject types
                will be eligible for warnings.

                If a library module is both compiled as a local dependency of an app and is
                distributed as a stand-alone library artifact, this issue should not be enabled
                as it will encourage brittle libraries that can introduce binary incompatibilities
                in the form of a kotlin.NoWhenBranchMatchedException. Lint cannot guarantee that
                libraries are not distributed this way. The lint check is enabled by
                default because this is a very unorthodox project structure.

                :app gradle module (has lint.checkDependencies = true), depends on :library
                   |
                   :library gradle module (this module is also published as a library outside of app module use)

                Disable the check for the library module in this case.
            """,
            moreInfo = "https://kotlinlang.org/docs/control-flow.html#when-expression",
            category = Category.CORRECTNESS,
            severity = Severity.WARNING,
            implementation = Implementation(
                FiniteWhenCasesDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            ),
            androidSpecific = true, // true because of the escape hatches in filterIncident
        )
    }
}
