package com.faithlife.lint

import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.client.api.IssueRegistry as ApiIssueRegistry

class IssueRegistry : ApiIssueRegistry() {
    override val vendor = Vendor(
        vendorName = "Faithlife",
        identifier = "android-lint",
        feedbackUrl = "https://github.com/Faithlife/AndroidLint/issues",
    )

    override val issues = listOf(
        ErrorCatchDetector.ISSUE_CATCH_TOO_GENERIC,
        ErrorCatchDetector.ISSUE_ERROR_CAUGHT,
        FiniteWhenCasesDetector.ISSUE,
        ForEachFunctionDetector.ISSUE,
        IndirectSuperCallDetector.ISSUE,
        ObsoleteDateInstantiationDetector.ISSUE,
        RedundantCoroutineScopeDetector.ISSUE,
        SimpleDateFormatDetector.ISSUE,
        SingleApostropheDetector.ISSUE,
        UnstyledTextComposableDetector.ISSUE_TEXT_COMPOSABLE_MATERIAL_TYPOGRAPHY,
        UnstyledTextComposableDetector.ISSUE_TEXT_COMPOSABLE_NO_STYLE,
    )

    override val api: Int = CURRENT_API
}
