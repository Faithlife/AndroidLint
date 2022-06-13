package com.faithlife.lint

import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.client.api.IssueRegistry as ApiIssueRegistry

@Suppress("UnstableApiUsage")
class IssueRegistry : ApiIssueRegistry() {
    override val vendor = Vendor(
        vendorName = "Faithlife",
        identifier = "android-lint",
        feedbackUrl = "https://github.com/Faithlife/AndroidLint/issues",
        contact = "mobile@faithlife.com"
    )

    override val issues = listOf(
        ObsoleteDateInstantiationDetector.ISSUE,
        RedundantCoroutineScopeDetector.ISSUE,
        SimpleDateFormatDetector.ISSUE,
        SingleApostropheDetector.ISSUE,
    )

    override val api: Int = CURRENT_API
}
