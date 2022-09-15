package com.faithlife.lint

import com.android.resources.ResourceFolderType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.TextFormat
import com.android.tools.lint.detector.api.XmlContext
import com.android.utils.text
import org.w3c.dom.Element

@Suppress("UnstableApiUsage")
class SingleApostropheDetector : ResourceXmlDetector() {

    override fun appliesTo(folderType: ResourceFolderType) = folderType == ResourceFolderType.VALUES

    override fun getApplicableElements() = mutableListOf("string")

    override fun visitElement(context: XmlContext, element: Element) {
        try {
            if (SINGLE_APOSTROPHE_REGEX.containsMatchIn(element.textContent)) {
                val fix = LintFix.create()
                    .replace()
                    .pattern("\\\\?'")
                    .with("’")
                    .build()

                context.report(
                    ISSUE,
                    element,
                    context.getLocation(element),
                    ISSUE.getBriefDescription(TextFormat.RAW),
                    fix
                )
            }
        } catch (e: NullPointerException) {
            context.log(
                e,
                """
                |File: ${context.file.absolutePath}
                |Text: ${element.text()} Element: $element
                """.trimMargin()
            )
        }
    }

    companion object {
        private val SINGLE_APOSTROPHE_REGEX = Regex("\\b[a-zA-Z0-9]+\\\\?'[a-zA-Z0-9]*")

        val ISSUE = Issue.create(
            "UseUnicodeApostrophe",
            "Prefer unicode apostrophes",
            "This check enforces the use of unicode punctuation for single quotation uses " +
                "like possessive parts of speech or contractions.",
            Implementation(SingleApostropheDetector::class.java, Scope.RESOURCE_FILE_SCOPE),
            "https://wiki.lrscorp.net/Use_Unicode_Punctuation_Characters",
            Category.TYPOGRAPHY,
            severity = Severity.WARNING,
        )
    }
}
