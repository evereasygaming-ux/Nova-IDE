package com.example.model

enum class SupportedLanguage(
    val id: String,
    val displayName: String,
    val extension: String,
    val compilerName: String,
    val version: String,
    val defaultFileName: String
) {
    PYTHON("python", "Python", ".py", "CPython 3.12 (Embedded bytecode runtime)", "3.12.4", "main.py"),
    CPP("cpp", "C++", ".cpp", "Clang / LLVM 18 (Cross-compilation engine)", "18.1.3", "main.cpp"),
    RUST("rust", "Rust", ".rs", "rustc / Cargo Embedded (LLVM codegen)", "1.79.0", "main.rs"),
    KOTLIN("kotlin", "Kotlin", ".kt", "kotlinc 2.2 / Native Embedded", "2.2.10", "Main.kt"),
    JAVA("java", "Java", ".java", "OpenJDK 21 (javac compiler)", "21.0.3", "Main.java"),
    SHELL("bash", "Bash / Shell", ".sh", "GNU Bash / Busybox Mobile Subsystem", "5.2.21", "deploy.sh")
}

data class Workspace(
    val id: String,
    val name: String,
    val rootPath: String,
    val description: String,
    val files: List<SourceFile> = emptyList(),
    val activeFileIndex: Int = 0,
    val defaultLanguage: SupportedLanguage = SupportedLanguage.PYTHON
)

data class VirtualEnvironment(
    val id: String,
    val name: String,
    val language: SupportedLanguage,
    val path: String,
    val packages: List<InstalledPackage> = emptyList(),
    val isActive: Boolean = false,
    val pythonVersion: String = "3.12.4"
)

data class InstalledPackage(
    val name: String,
    val version: String,
    val size: String,
    val isSystem: Boolean = false
)

data class SourceFile(
    val name: String,
    val path: String,
    val content: String,
    val language: SupportedLanguage,
    val isModified: Boolean = false,
    val sizeBytes: Long = 0L,
    val lastModified: Long = System.currentTimeMillis()
)

data class TerminalLine(
    val id: Long = System.nanoTime(),
    val text: String,
    val type: TerminalLineType = TerminalLineType.OUTPUT,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TerminalLineType {
    INPUT,
    OUTPUT,
    SUCCESS,
    ERROR,
    SYSTEM,
    AI_STREAM
}

data class ExecutionResult(
    val success: Boolean,
    val exitCode: Int,
    val output: String,
    val errorOutput: String? = null,
    val executionTimeMs: Long = 0L,
    val memoryUsageMb: Double = 0.0,
    val language: SupportedLanguage
)

data class OpenCodeMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val codeSnippet: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
)

enum class MessageSender {
    USER,
    OPENCODE_AI,
    SYSTEM
}

data class CodeSymbol(
    val name: String,
    val kind: SymbolKind,
    val line: Int,
    val signature: String,
    val documentation: String? = null
)

enum class SymbolKind {
    FUNCTION,
    STRUCT_OR_CLASS,
    VARIABLE,
    CONSTANT,
    KEYWORD,
    MACRO
}

data class CompletionItem(
    val label: String,
    val insertText: String,
    val kind: SymbolKind,
    val detail: String,
    val documentation: String? = null
)

data class DiagnosticIssue(
    val line: Int,
    val column: Int,
    val message: String,
    val severity: DiagnosticSeverity
)

enum class DiagnosticSeverity {
    ERROR,
    WARNING,
    INFO
}
