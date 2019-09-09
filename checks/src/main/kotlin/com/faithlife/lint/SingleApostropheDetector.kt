package com.faithlife.lint

import com.android.resources.ResourceFolderType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Detector.XmlScanner
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Element

class SingleApostropheDetector : Detector(), XmlScanner {

    override fun appliesTo(folderType: ResourceFolderType) = folderType == ResourceFolderType.VALUES

    override fun getApplicableElements(): MutableCollection<String> = mutableListOf("string")

    override fun visitElement(context: XmlContext, element: Element) {
        if (SINGLE_APOSTROPHE_REGEX.containsMatchIn(element.firstChild.nodeValue)) {
            val fix = LintFix.create()
                .replace()
                .text("'")
                .with("’")
                .build()

            context.report(
                ISSUE,
                element,
                context.getLocation(element),
                "https://wiki.lrscorp.net/Use_Unicode_Punctuation_Characters",
                fix
            )
        }
    }

    companion object {
        private val SINGLE_APOSTROPHE_REGEX = Regex("\\b[a-zA-Z0-9]+'[a-zA-Z0-9]*")

        @Suppress("UnstableApiUsage")
        val ISSUE = Issue.create(
            // ID: used in @SuppressLint warnings etc
            "UseUnicodeApostrophe",

            // Title -- shown in the IDE's preference dialog, as category headers in the
            // Analysis results window, etc
            "Prefer unicode punctuation",

            // Full explanation of the issue; you can use some markdown markup such as
            // `monospace`, *italic*, and **bold**.
            """This check enforces the use of unicode punctuation for single quotation uses
                like possessive parts of speech or contractions.""".trimIndent(),
            Category.TYPOGRAPHY,
            6,
            Severity.WARNING,
            Implementation(SingleApostropheDetector::class.java, Scope.RESOURCE_FILE_SCOPE)
        )
    }
}
