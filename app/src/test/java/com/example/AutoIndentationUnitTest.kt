package com.example

import com.example.engine.AutoIndentationEngine
import com.example.model.SupportedLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class AutoIndentationUnitTest {

    @Test
    fun testPythonColonAutoIndent() {
        val oldText = "def calculate():"
        val newText = "def calculate():\n"
        val processed = AutoIndentationEngine.processTextChange(oldText, newText, SupportedLanguage.PYTHON)
        assertEquals("def calculate():\n    ", processed)
    }

    @Test
    fun testKotlinBraceAutoIndent() {
        val oldText = "fun main() {"
        val newText = "fun main() {\n"
        val processed = AutoIndentationEngine.processTextChange(oldText, newText, SupportedLanguage.KOTLIN)
        assertEquals("fun main() {\n    ", processed)
    }

    @Test
    fun testJavaBraceAutoIndent() {
        val oldText = "public class App {"
        val newText = "public class App {\n"
        val processed = AutoIndentationEngine.processTextChange(oldText, newText, SupportedLanguage.JAVA)
        assertEquals("public class App {\n    ", processed)
    }

    @Test
    fun testRustMatchAutoIndent() {
        val oldText = "match result {"
        val newText = "match result {\n"
        val processed = AutoIndentationEngine.processTextChange(oldText, newText, SupportedLanguage.RUST)
        assertEquals("match result {\n    ", processed)
    }

    @Test
    fun testFormatCodeIndentation() {
        val unformatted = "fun main() {\nprintln(\"Hello\")\n}"
        val formatted = AutoIndentationEngine.formatCode(unformatted, SupportedLanguage.KOTLIN)
        val expected = "fun main() {\n    println(\"Hello\")\n}"
        assertEquals(expected, formatted)
    }
}
