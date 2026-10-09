package com.example

import com.example.engine.EnvironmentBootstrapManager
import com.example.engine.LinuxEmbeddedRuntime
import com.example.engine.ProjectIndexEngine
import com.example.engine.SyntaxHighlighter
import com.example.model.SourceFile
import com.example.model.SupportedLanguage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class NovaCodeIdeUnitTest {

    private val runtime = LinuxEmbeddedRuntime()
    private val indexEngine = ProjectIndexEngine()
    private val bootstrapManager = EnvironmentBootstrapManager()

    @Test
    fun testPythonEmbeddedRuntimeExecution() = runBlocking {
        val pythonCode = """
x = 21
y = 2
result = x * y
print(f"Computed result is {result}")
        """.trimIndent()

        val result = runtime.execute(SupportedLanguage.PYTHON, pythonCode)
        assertTrue(result.success)
        assertEquals(0, result.exitCode)
        assertTrue(result.output.contains("Computed result is 42"))
    }

    @Test
    fun testCppEmbeddedRuntimeExecution() = runBlocking {
        val cppCode = """
#include <iostream>
int main() {
    std::cout << "NovaCode Embedded C++ OK" << std::endl;
    return 0;
}
        """.trimIndent()

        val result = runtime.execute(SupportedLanguage.CPP, cppCode)
        assertTrue(result.success)
        assertEquals(0, result.exitCode)
        assertTrue(result.output.contains("NovaCode Embedded C++ OK"))
    }

    @Test
    fun testRustEmbeddedRuntimeExecution() = runBlocking {
        val rustCode = """
fn main() {
    println!("Hello from safe Rust concurrency!");
}
        """.trimIndent()

        val result = runtime.execute(SupportedLanguage.RUST, rustCode)
        assertTrue(result.success)
        assertEquals(0, result.exitCode)
        assertTrue(result.output.contains("Hello from safe Rust concurrency!"))
    }

    @Test
    fun testSubMillisecondProjectIndexEngine() = runBlocking {
        val file1 = SourceFile("main.py", "/workspace/main.py", "def compute_loss(): pass", SupportedLanguage.PYTHON)
        val file2 = SourceFile("server.rs", "/workspace/server.rs", "fn handle_connection() {}", SupportedLanguage.RUST)

        indexEngine.reindexAll(listOf(file1, file2))

        val symbolMatches = indexEngine.searchSymbol("compute")
        assertEquals(1, symbolMatches.size)
        assertEquals("compute_loss", symbolMatches[0].symbol.name)

        val fullTextMatches = indexEngine.fullTextSearch("connection")
        assertEquals(1, fullTextMatches.size)
        assertEquals("server.rs", fullTextMatches[0].fileName)
    }

    @Test
    fun testVirtualEnvBootstrap() = runBlocking {
        val envs = bootstrapManager.getEnvironments()
        assertTrue(envs.isNotEmpty())

        val pyEnv = envs.first { it.language == SupportedLanguage.PYTHON }
        assertTrue(pyEnv.packages.any { it.name == "numpy" })

        val newEnv = bootstrapManager.createEnvironment("test-env", SupportedLanguage.RUST)
        assertEquals("test-env", newEnv.name)
        assertTrue(newEnv.packages.any { it.name == "cargo" })
    }

    @Test
    fun testSyntaxHighlighter() {
        val rustCode = "fn main() { let x = 42; println!(\"test\"); }"
        val highlighted = SyntaxHighlighter.highlight(rustCode, SupportedLanguage.RUST)
        assertEquals(rustCode, highlighted.text)
        assertTrue(highlighted.spanStyles.isNotEmpty())
    }
}
