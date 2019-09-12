package com.faithlife.lint;

import com.android.tools.lint.client.api.IssueRegistry as ApiIssueRegistry;
import com.android.tools.lint.detector.api.CURRENT_API

class IssueRegistry : ApiIssueRegistry() {
    override val issues = listOf(SingleApostropheDetector.ISSUE)
    override val api: Int = CURRENT_API
}
