package com.example.engine

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.model.SupportedLanguage

object SyntaxHighlighter {

    private val pythonKeywords = setOf(
        "and", "as", "assert", "async", "await", "break", "class", "continue",
        "def", "del", "elif", "else", "except", "finally", "for", "from",
        "global", "if", "import", "in", "is", "lambda", "nonlocal", "not",
        "or", "pass", "raise", "return", "try", "while", "with", "yield",
        "True", "False", "None"
    )

    private val cppKeywords = setOf(
        "auto", "bool", "break", "case", "catch", "char", "class", "const",
        "constexpr", "continue", "default", "delete", "do", "double", "else",
        "enum", "explicit", "export", "extern", "false", "float", "for",
        "friend", "goto", "if", "inline", "int", "long", "mutable", "namespace",
        "new", "noexcept", "nullptr", "operator", "private", "protected",
        "public", "register", "reinterpret_cast", "return", "short", "signed",
        "sizeof", "static", "static_cast", "struct", "switch", "template",
        "this", "throw", "true", "try", "typedef", "typeid", "typename",
        "union", "unsigned", "using", "virtual", "void", "volatile", "while",
        "std", "vector", "string", "cout", "endl", "cin"
    )

    private val rustKeywords = setOf(
        "as", "async", "await", "break", "const", "continue", "crate", "dyn",
        "else", "enum", "extern", "false", "fn", "for", "if", "impl", "in",
        "let", "loop", "match", "mod", "move", "mut", "pub", "ref", "return",
        "self", "Self", "static", "struct", "super", "trait", "true", "type",
        "unsafe", "use", "where", "while", "println", "vec", "Option", "Result",
        "Some", "None", "Ok", "Err", "String", "i32", "u32", "i64", "u64", "f64"
    )

    private val kotlinKeywords = setOf(
        "as", "as?", "break", "class", "continue", "do", "else", "false", "for",
        "fun", "if", "in", "!in", "is", "!is", "null", "object", "package",
        "return", "super", "this", "throw", "true", "try", "typealias", "val",
        "var", "when", "while", "by", "catch", "constructor", "delegate",
        "dynamic", "field", "file", "finally", "get", "import", "init", "param",
        "property", "receiver", "set", "setparam", "where", "actual", "abstract",
        "annotation", "companion", "const", "crossinline", "data", "enum", "expect",
        "external", "final", "infix", "inline", "inner", "internal", "lateinit",
        "noinline", "open", "operator", "out", "override", "private", "protected",
        "public", "reified", "sealed", "suspend", "tailrec", "vararg"
    )

    private val javaKeywords = setOf(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new",
        "package", "private", "protected", "public", "return", "short", "static",
        "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while", "true", "false", "null",
        "String", "System", "List", "Map", "Set", "ArrayList", "HashMap"
    )

    fun highlight(
        code: String,
        language: SupportedLanguage,
        colorScheme: SyntaxColorScheme = SyntaxColorScheme.CyberDark
    ): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            val lines = code.lines()
            var currentOffset = 0

            lines.forEach { line ->
                val lineLength = line.length

                // Check comments first
                val commentPrefix = when (language) {
                    SupportedLanguage.PYTHON, SupportedLanguage.SHELL -> "#"
                    SupportedLanguage.CPP, SupportedLanguage.RUST, SupportedLanguage.KOTLIN, SupportedLanguage.JAVA -> "//"
                }

                val commentIdx = line.indexOf(commentPrefix)
                val codeSegment = if (commentIdx != -1) line.substring(0, commentIdx) else line

                // Highlight strings inside codeSegment
                val stringRegex = Regex("""("[^"\\]*(?:\\.[^"\\]*)*"|'[^'\\]*(?:\\.[^'\\]*)*')""")
                stringRegex.findAll(codeSegment).forEach { match ->
                    val start = currentOffset + match.range.first
                    val end = currentOffset + match.range.last + 1
                    addStyle(SpanStyle(color = colorScheme.stringColor), start, end)
                }

                // Highlight numbers
                val numberRegex = Regex("""\b(\d+(\.\d+)?([eE][+-]?\d+)?|0x[0-9a-fA-F]+)\b""")
                numberRegex.findAll(codeSegment).forEach { match ->
                    val start = currentOffset + match.range.first
                    val end = currentOffset + match.range.last + 1
                    addStyle(SpanStyle(color = colorScheme.numberColor), start, end)
                }

                // Highlight words (keywords, types, functions)
                val wordRegex = Regex("""\b([A-Za-z_][A-Za-z0-9_]*)\b""")
                wordRegex.findAll(codeSegment).forEach { match ->
                    val word = match.value
                    val start = currentOffset + match.range.first
                    val end = currentOffset + match.range.last + 1

                    // Is it inside a string literal?
                    val isInsideString = stringRegex.findAll(codeSegment).any { strMatch ->
                        match.range.first >= strMatch.range.first && match.range.last <= strMatch.range.last
                    }

                    if (!isInsideString) {
                        when (language) {
                            SupportedLanguage.PYTHON -> {
                                if (pythonKeywords.contains(word)) {
                                    addStyle(SpanStyle(color = colorScheme.keywordColor, fontWeight = FontWeight.Bold), start, end)
                                } else if (word in setOf("print", "len", "range", "int", "str", "float", "list", "dict", "set")) {
                                    addStyle(SpanStyle(color = colorScheme.functionColor), start, end)
                                }
                            }
                            SupportedLanguage.KOTLIN -> {
                                if (kotlinKeywords.contains(word)) {
                                    addStyle(SpanStyle(color = colorScheme.keywordColor, fontWeight = FontWeight.Bold), start, end)
                                } else if (word in setOf("Int", "String", "Boolean", "Float", "Double", "List", "Map", "Set")) {
                                    addStyle(SpanStyle(color = colorScheme.typeColor), start, end)
                                } else if (line.substring(match.range.last + 1).trimStart().startsWith("(")) {
                                    addStyle(SpanStyle(color = colorScheme.functionColor), start, end)
                                }
                            }
                            SupportedLanguage.JAVA -> {
                                if (javaKeywords.contains(word)) {
                                    addStyle(SpanStyle(color = colorScheme.keywordColor, fontWeight = FontWeight.Bold), start, end)
                                } else if (word in setOf("String", "Integer", "System", "Object", "List", "Map", "boolean", "int", "void")) {
                                    addStyle(SpanStyle(color = colorScheme.typeColor), start, end)
                                } else if (line.substring(match.range.last + 1).trimStart().startsWith("(")) {
                                    addStyle(SpanStyle(color = colorScheme.functionColor), start, end)
                                }
                            }
                            SupportedLanguage.CPP -> {
                                if (cppKeywords.contains(word)) {
                                    addStyle(SpanStyle(color = colorScheme.keywordColor, fontWeight = FontWeight.Bold), start, end)
                                } else if (word.startsWith("std::") || word in setOf("vector", "string", "int", "float", "double", "void", "bool")) {
                                    addStyle(SpanStyle(color = colorScheme.typeColor), start, end)
                                } else if (line.substring(match.range.last + 1).trimStart().startsWith("(")) {
                                    addStyle(SpanStyle(color = colorScheme.functionColor), start, end)
                                }
                            }
                            SupportedLanguage.RUST -> {
                                if (rustKeywords.contains(word)) {
                                    addStyle(SpanStyle(color = colorScheme.keywordColor, fontWeight = FontWeight.Bold), start, end)
                                } else if (word in setOf("Option", "Result", "String", "Vec", "i32", "u64", "usize", "bool")) {
                                    addStyle(SpanStyle(color = colorScheme.typeColor), start, end)
                                } else if (line.substring(match.range.last + 1).trimStart().startsWith("!")) {
                                    addStyle(SpanStyle(color = colorScheme.macroColor, fontWeight = FontWeight.SemiBold), start, end)
                                } else if (line.substring(match.range.last + 1).trimStart().startsWith("(")) {
                                    addStyle(SpanStyle(color = colorScheme.functionColor), start, end)
                                }
                            }
                            SupportedLanguage.SHELL -> {
                                if (word in setOf("echo", "cd", "ls", "export", "source", "if", "then", "fi", "for", "in", "do", "done")) {
                                    addStyle(SpanStyle(color = colorScheme.keywordColor, fontWeight = FontWeight.Bold), start, end)
                                }
                            }
                        }
                    }
                }

                // Highlight full comment segment
                if (commentIdx != -1) {
                    val cStart = currentOffset + commentIdx
                    val cEnd = currentOffset + lineLength
                    addStyle(SpanStyle(color = colorScheme.commentColor, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), cStart, cEnd)
                }

                // Add newline offset
                currentOffset += lineLength + 1
            }
        }
    }
}
