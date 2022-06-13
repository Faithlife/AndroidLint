package com.faithlife.lint

import com.android.resources.ResourceFolderType
import com.android.tools.lint.detector.api.*
import com.android.utils.text
import org.w3c.dom.Element

@Suppress("UnstableApiUsage")
class SingleApostropheDetector : ResourceXmlDetector() {

    override fun appliesTo(folderType: ResourceFolderType) = folderType == ResourceFolderType.VALUES

    override fun getApplicableElements(): MutableCollection<String> = mutableListOf("string")

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
                    "Prefer unicode apostrophes.\n\nhttps://wiki.lrscorp.net/Use_Unicode_Punctuation_Characters",
                    fix
                )
            }
        } catch (e: NullPointerException) {
            println("File: ${context.file.absolutePath}\nText: ${element.text()} Element: $element")
        }
    }

    companion object {
        private val SINGLE_APOSTROPHE_REGEX = Regex("\\b[a-zA-Z0-9]+\\\\?'[a-zA-Z0-9]*")

        @Suppress("UnstableApiUsage")
        val ISSUE = Issue.create(
            // ID: used in @SuppressLint warnings etc
            "UseUnicodeApostrophe",

            // Title -- shown in the IDE's preference dialog, as category headers in the
            // Analysis results window, etc
            "Prefer unicode punctuation",

            // Full explanation of the issue; you can use some markdown markup such as
            // `monospace`, *italic*, and **bold**.
            "This check enforces the use of unicode punctuation for single quotation uses " +
                "like possessive parts of speech or contractions.",
            Category.TYPOGRAPHY,
            6,
            Severity.WARNING,
            Implementation(SingleApostropheDetector::class.java, Scope.RESOURCE_FILE_SCOPE)
        )
    }
}
