package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test

@Suppress("UnstableApiUsage")
class SingleApostropheDetectorTests : LintDetectorTest() {
    override fun getDetector() = SingleApostropheDetector()
    override fun getIssues() = listOf(SingleApostropheDetector.ISSUE)

    @Test
    fun `test pre-word single apostrophe ignored`() {
        val source = "<string name=\"a_bad_string\">\\'ASCII punctuation</string>"
        lint().files(xml("res/values/strings.xml", source))
            .run()
            .expectClean()
    }

    @Test
    fun `test mid-word single apostrophe detected`() {
        val source = "<string name=\"a_bad_string\">ASCII\'s punctuation</string>"
        lint().files(xml("res/values/strings.xml", source))
            .run()
            .expect(
                """res/values/strings.xml:1: Warning: Prefer unicode apostrophes [UseUnicodeApostrophe]
                |<string name="a_bad_string">ASCII's punctuation</string>
                |~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings
                """.trimMargin()
            )
    }

    @Test
    fun `test end-word single apostrophe detected`() {
        val source = "<string name=\"a_bad_string\">ASCIIs\' punctuation</string>"
        lint().files(xml("res/values/strings.xml", source))
            .run()
            .expect(
                """res/values/strings.xml:1: Warning: Prefer unicode apostrophes [UseUnicodeApostrophe]
                |<string name="a_bad_string">ASCIIs' punctuation</string>
                |~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings
                """.trimMargin()
            )
    }

    @Test
    fun `test apostrophe escaped at the end of a word`() {
        val source = "<string name=\"a_bad_string\">ASCII\\'s punctuation</string>"
        lint().files(xml("res/values/strings.xml", source))
            .run()
            .expect(
                """res/values/strings.xml:1: Warning: Prefer unicode apostrophes [UseUnicodeApostrophe]
                |<string name="a_bad_string">ASCII\'s punctuation</string>
                |~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings
                """.trimMargin()
            )
    }

    @Test
    fun `test clean`() {
        val source = "<string name=\"a_fine_string\">ASCII punctuation</string>"
        lint().files(xml("res/values/strings.xml", source))
            .run()
            .expectClean()
    }

    @Test
    fun `test apostrophe fix`() {
        val source = "<string name=\"a_bad_string\">ASCII\\'s punctuation</string>"
        lint().files(xml("res/values/strings.xml", source))
            .run()
            .checkFix(null, xml("res/values/strings.xml", source.replace("\\'", "’")))
    }

    @Test
    fun `test escaped apostrophe fix`() {
        val source = "<string name=\"a_bad_string\">ASCII's punctuation</string>"
        lint().files(xml("res/values/strings.xml", source))
            .run()
            .checkFix(null, xml("res/values/strings.xml", source.replace('\'', '’')))
    }
}
