package com.faithlife.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.Test

class SingleApostropheDetectorTests : LintDetectorTest() {
    override fun getDetector() = SingleApostropheDetector()
    override fun getIssues() = listOf(SingleApostropheDetector.ISSUE)

    @Test
    fun testMidWordSingleApostropheDetected() {
        lint().files(xml("res/values/strings.xml", "<string name=\"a_bad_string\">ASCII's punctuation</string>"))
            .run()
            .expect("""res/values/strings.xml:1: Warning: https://wiki.lrscorp.net/Use_Unicode_Punctuation_Characters [UseUnicodeApostrophe]
                |<string name="a_bad_string">ASCII's punctuation</string>
                |~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin())
    }

    @Test
    fun testEndWordSingleApostropheDetected() {
        lint().files(xml("res/values/strings.xml", "<string name=\"a_bad_string\">ASCIIs' punctuation</string>"))
            .run()
            .expect("""res/values/strings.xml:1: Warning: https://wiki.lrscorp.net/Use_Unicode_Punctuation_Characters [UseUnicodeApostrophe]
                |<string name="a_bad_string">ASCIIs' punctuation</string>
                |~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin())
    }

    @Test
    fun testNoSingleApostropheDetected() {
        lint().files(xml("res/values/strings.xml", "<string name=\"a_fine_string\">ASCII punctuation</string>"))
            .run()
            .expect("""No warnings.""".trimMargin())
    }
}
