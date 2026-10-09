package com.example.engine

import com.example.model.SupportedLanguage

/**
 * Intelligent Language-Aware Auto-Indenter for the CodeEditor.
 * Detects language-specific style guides (PEP 8 for Python, Google Style for Java/C++,
 * Kotlin Official Guidelines, and Rustfmt conventions) and automatically calculates
 * the appropriate indent whenever a newline is entered or indentation is triggered.
 */
object AutoIndentationEngine {

    data class IndentConfig(
        val indentUnit: String = "    ", // standard 4 spaces
        val triggersBlockIndent: List<String> = emptyList(),
        val autoClosePairs: Map<Char, Char> = mapOf(
            '{' to '}',
            '(' to ')',
            '[' to ']',
            '"' to '"',
            '\'' to '\''
        )
    )

    fun getLanguageConfig(language: SupportedLanguage): IndentConfig {
        return when (language) {
            SupportedLanguage.PYTHON -> IndentConfig(
                indentUnit = "    ", // PEP 8: 4 spaces
                triggersBlockIndent = listOf(":")
            )
            SupportedLanguage.KOTLIN -> IndentConfig(
                indentUnit = "    ", // Kotlin style guide: 4 spaces
                triggersBlockIndent = listOf("{", "->", "=")
            )
            SupportedLanguage.JAVA -> IndentConfig(
                indentUnit = "    ", // Standard Java style: 4 spaces
                triggersBlockIndent = listOf("{")
            )
            SupportedLanguage.CPP -> IndentConfig(
                indentUnit = "    ", // Standard C++: 4 spaces
                triggersBlockIndent = listOf("{")
            )
            SupportedLanguage.RUST -> IndentConfig(
                indentUnit = "    ", // Rustfmt: 4 spaces
                triggersBlockIndent = listOf("{", "=>")
            )
            SupportedLanguage.SHELL -> IndentConfig(
                indentUnit = "    ",
                triggersBlockIndent = listOf("then", "do", "{")
            )
        }
    }

    /**
     * Inspects previous text and newly typed text.
     * If a newline was just inserted, detects the previous line's indent level,
     * checks for syntax block openers (e.g., colon in Python, braces in Java/Kotlin/C++/Rust),
     * and returns the auto-indented text.
     */
    fun processTextChange(
        oldText: String,
        newText: String,
        language: SupportedLanguage
    ): String {
        // Only process if user pressed newline (newText is longer by 1 and ends with '\n')
        if (newText.length == oldText.length + 1 && newText.endsWith("\n")) {
            val lines = oldText.lines()
            val lastLine = lines.lastOrNull() ?: ""
            val config = getLanguageConfig(language)

            // Extract existing leading whitespace on the line being completed
            val leadingSpacesCount = lastLine.takeWhile { it == ' ' || it == '\t' }.length
            val baseIndent = lastLine.take(leadingSpacesCount)
            val trimmedLastLine = lastLine.trim()

            var extraIndent = ""
            when (language) {
                SupportedLanguage.PYTHON -> {
                    // Python PEP 8: line ending in ':' indents +4 spaces
                    if (trimmedLastLine.endsWith(":")) {
                        extraIndent = config.indentUnit
                    }
                }
                SupportedLanguage.KOTLIN, SupportedLanguage.JAVA, SupportedLanguage.CPP, SupportedLanguage.RUST -> {
                    // Block opening with '{' or '->' or '=>'
                    if (trimmedLastLine.endsWith("{") || trimmedLastLine.endsWith("->") || trimmedLastLine.endsWith("=>")) {
                        extraIndent = config.indentUnit
                    }
                }
                SupportedLanguage.SHELL -> {
                    if (trimmedLastLine.endsWith("then") || trimmedLastLine.endsWith("do") || trimmedLastLine.endsWith("{")) {
                        extraIndent = config.indentUnit
                    }
                }
            }

            return newText + baseIndent + extraIndent
        }

        return newText
    }

    /**
     * Formats an entire code buffer according to language style guidelines.
     */
    fun formatCode(code: String, language: SupportedLanguage): String {
        val config = getLanguageConfig(language)
        val lines = code.lines()
        val formattedLines = mutableListOf<String>()
        var currentIndentLevel = 0

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                formattedLines.add("")
                continue
            }

            // Decrease indent before printing closing bracket
            if (trimmed.startsWith("}") || trimmed.startsWith("]") || trimmed.startsWith(")")) {
                currentIndentLevel = (currentIndentLevel - 1).coerceAtLeast(0)
            } else if (language == SupportedLanguage.PYTHON && (trimmed.startsWith("elif ") || trimmed.startsWith("else:") || trimmed.startsWith("except:") || trimmed.startsWith("finally:"))) {
                currentIndentLevel = (currentIndentLevel - 1).coerceAtLeast(0)
            }

            val indent = config.indentUnit.repeat(currentIndentLevel)
            formattedLines.add(indent + trimmed)

            // Increase indent after block openers
            when (language) {
                SupportedLanguage.PYTHON -> {
                    if (trimmed.endsWith(":")) {
                        currentIndentLevel++
                    }
                }
                SupportedLanguage.KOTLIN, SupportedLanguage.JAVA, SupportedLanguage.CPP, SupportedLanguage.RUST -> {
                    if (trimmed.endsWith("{")) {
                        currentIndentLevel++
                    }
                }
                SupportedLanguage.SHELL -> {
                    if (trimmed.endsWith("then") || trimmed.endsWith("do") || trimmed.endsWith("{")) {
                        currentIndentLevel++
                    }
                }
            }
        }

        return formattedLines.joinToString("\n")
    }
}
