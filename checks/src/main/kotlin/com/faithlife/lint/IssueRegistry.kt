package com.faithlife.lint;

import com.android.tools.lint.client.api.IssueRegistry as ApiIssueRegistry;
import com.android.tools.lint.detector.api.Issue;

class IssueRegistry : ApiIssueRegistry() {
    override val issues = listOf(SingleApostropheDetector.ISSUE)
}
