package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.OpenCodeAiService
import com.example.engine.EnvironmentBootstrapManager
import com.example.engine.LinuxEmbeddedRuntime
import com.example.engine.ProjectIndexEngine
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IdeUiState(
    val currentTab: IdeTab = IdeTab.EDITOR,
    val openFiles: List<SourceFile> = emptyList(),
    val activeFileIndex: Int = 0,
    val isRunning: Boolean = false,
    val terminalLines: List<TerminalLine> = emptyList(),
    val terminalInput: String = "",
    val environments: List<VirtualEnvironment> = emptyList(),
    val activeEnvId: String = "env_py_core",
    val workspaces: List<Workspace> = emptyList(),
    val activeWorkspaceId: String = "ws_default",
    val searchQuery: String = "",
    val searchResults: List<ProjectIndexEngine.SearchResult> = emptyList(),
    val isRegexSearch: Boolean = false,
    val aiMessages: List<OpenCodeMessage> = emptyList(),
    val isAiThinking: Boolean = false,
    val isVoiceOrbActive: Boolean = false,
    val voiceOrbMode: VoiceOrbState = VoiceOrbState.IDLE,
    val voiceTranscript: String = "",
    val completionSuggestions: List<CompletionItem> = emptyList(),
    val diagnostics: List<DiagnosticIssue> = emptyList(),
    val cursorLine: Int = 1,
    val cursorColumn: Int = 1,
    val totalLines: Int = 1,
    val fpsMeter: Int = 60,
    val memoryUsageMb: Double = 42.4
)

enum class IdeTab {
    EDITOR,
    TERMINAL,
    ENVIRONMENTS,
    SEARCH_INDEX,
    OPENCODE_AI
}

enum class VoiceOrbState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING
}

class IdeViewModel : ViewModel() {

    private val runtime = LinuxEmbeddedRuntime()
    private val indexEngine = ProjectIndexEngine()
    private val bootstrapManager = EnvironmentBootstrapManager()
    private val aiService = OpenCodeAiService()

    private val _uiState = MutableStateFlow(IdeUiState())
    val uiState: StateFlow<IdeUiState> = _uiState.asStateFlow()

    init {
        loadInitialProject()
    }

    private fun loadInitialProject() {
        val initialFiles = listOf(
            SourceFile(
                name = "main.py",
                path = "/workspace/src/main.py",
                language = SupportedLanguage.PYTHON,
                content = """# NovaCode Mobile IDE - High Performance Embedded Python 3.12
import math

def calculate_neural_weights(layers):
    print("Initializing embedded neural tensor layers...")
    weights = []
    for i in range(layers):
        score = i * 42
        weights.append(score)
        print(f"Layer {i}: Initialized weight tensor -> {score}")
    return weights

print("--- Starting NovaCode Linux Subsystem ---")
layers = 5
results = calculate_neural_weights(layers)
print(f"Completed initialization of {layers} layers.")
print("Embedded runtime status: OK 60 FPS")
"""
            ),
            SourceFile(
                name = "main.cpp",
                path = "/workspace/src/main.cpp",
                language = SupportedLanguage.CPP,
                content = """// NovaCode Embedded C++20 (LLVM/Clang Toolchain)
#include <iostream>
#include <vector>
#include <numeric>

int main() {
    std::cout << "NovaCode Embedded Clang++ 18.1 Subsystem" << std::endl;
    std::vector<int> buffer = {10, 20, 30, 40, 50};
    int sum = 0;
    for (int n : buffer) {
        sum += n;
    }
    std::cout << "Computed Vector Accumulation: " << sum << std::endl;
    std::cout << "Zero allocation pass complete. EXIT_SUCCESS" << std::endl;
    return 0;
}
"""
            ),
            SourceFile(
                name = "main.rs",
                path = "/workspace/src/main.rs",
                language = SupportedLanguage.RUST,
                content = """// NovaCode Embedded Rust 1.79 Engine
fn main() {
    println!("NovaCode High-Throughput Safe Concurrency");
    let mut counter = 0;
    for i in 0..5 {
        counter += i * 10;
        println!("Thread worker cycle {}: accumulator = {}", i, counter);
    }
    println!("Borrow check verified: No memory leaks detected.");
}
"""
            ),
            SourceFile(
                name = "deploy.sh",
                path = "/workspace/deploy.sh",
                language = SupportedLanguage.SHELL,
                content = """#!/usr/bin/env bash
# NovaCode Android-Linux Container Deployment Script
echo "=== Bootstrapping Mobile Linux Toolchains ==="
uname -a
echo "Checking virtual environments..."
ls
echo "Deployment successful: All runtimes synchronized."
"""
            )
        )

        val defaultWorkspace = Workspace(
            id = "ws_default",
            name = "Default Multi-Lang Project",
            rootPath = "/workspace",
            description = "Embedded Python 3.12, C++20 Clang and Rust 1.79 development container",
            files = initialFiles,
            activeFileIndex = 0,
            defaultLanguage = SupportedLanguage.PYTHON
        )

        val envs = bootstrapManager.getEnvironments()
        val initialLines = listOf(
            TerminalLine(text = "Linux novacode-subsystem 6.6.21-android aarch64", type = TerminalLineType.SYSTEM),
            TerminalLine(text = "NovaCode Core Environment initialized. Ready.", type = TerminalLineType.SUCCESS),
            TerminalLine(text = "Workspace '${defaultWorkspace.name}' mounted at /workspace", type = TerminalLineType.SUCCESS),
            TerminalLine(text = "Virtualenv 'py312-ml-runtime' active on [aarch64]", type = TerminalLineType.SYSTEM)
        )

        val welcomeAi = OpenCodeMessage(
            sender = MessageSender.OPENCODE_AI,
            text = "Welcome to NovaCode Mobile Linux IDE! I am OpenCode AI. You can tap the 3D Orb or ask me to optimize, compile, and refactor code across Python, C++, and Rust."
        )

        _uiState.update {
            it.copy(
                openFiles = initialFiles,
                workspaces = listOf(defaultWorkspace),
                activeWorkspaceId = defaultWorkspace.id,
                environments = envs,
                terminalLines = initialLines,
                aiMessages = listOf(welcomeAi),
                totalLines = initialFiles[0].content.lines().size
            )
        }

        viewModelScope.launch {
            indexEngine.reindexAll(initialFiles)
            updateDiagnostics()
        }
    }

    val activeWorkspace: Workspace?
        get() {
            val state = _uiState.value
            return state.workspaces.find { it.id == state.activeWorkspaceId } ?: state.workspaces.firstOrNull()
        }

    val activeFile: SourceFile?
        get() {
            val state = _uiState.value
            return state.openFiles.getOrNull(state.activeFileIndex)
        }

    fun selectTab(tab: IdeTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectFile(index: Int) {
        if (index in _uiState.value.openFiles.indices) {
            _uiState.update {
                it.copy(
                    activeFileIndex = index,
                    totalLines = it.openFiles[index].content.lines().size,
                    cursorLine = 1,
                    cursorColumn = 1,
                    completionSuggestions = emptyList()
                )
            }
            // Sync with active workspace
            val currentWsId = _uiState.value.activeWorkspaceId
            val updatedWs = _uiState.value.workspaces.map { ws ->
                if (ws.id == currentWsId) ws.copy(activeFileIndex = index) else ws
            }
            _uiState.update { it.copy(workspaces = updatedWs) }
            updateDiagnostics()
        }
    }

    fun updateCode(newContent: String) {
        val currentIdx = _uiState.value.activeFileIndex
        val files = _uiState.value.openFiles.toMutableList()
        if (currentIdx in files.indices) {
            val updated = files[currentIdx].copy(
                content = newContent,
                isModified = true
            )
            files[currentIdx] = updated

            val currentWsId = _uiState.value.activeWorkspaceId
            val updatedWorkspaces = _uiState.value.workspaces.map { ws ->
                if (ws.id == currentWsId) ws.copy(files = files) else ws
            }

            _uiState.update {
                it.copy(
                    openFiles = files,
                    workspaces = updatedWorkspaces,
                    totalLines = newContent.lines().size
                )
            }

            viewModelScope.launch {
                indexEngine.indexFile(updated)
                updateDiagnostics()
            }
        }
    }

    fun updateCursor(line: Int, column: Int, currentPrefix: String = "") {
        _uiState.update {
            it.copy(
                cursorLine = line,
                cursorColumn = column
            )
        }

        activeFile?.let { file ->
            if (currentPrefix.isNotBlank()) {
                val completions = indexEngine.getCompletions(currentPrefix, file.language)
                _uiState.update { it.copy(completionSuggestions = completions) }
            } else {
                _uiState.update { it.copy(completionSuggestions = emptyList()) }
            }
        }
    }

    private fun updateDiagnostics() {
        activeFile?.let { file ->
            val issues = when (file.language) {
                SupportedLanguage.PYTHON -> runtime.validatePythonSyntax(file.content)
                SupportedLanguage.CPP -> runtime.validateCppSyntax(file.content)
                SupportedLanguage.RUST -> runtime.validateRustSyntax(file.content)
                SupportedLanguage.KOTLIN, SupportedLanguage.JAVA, SupportedLanguage.SHELL -> emptyList()
            }
            _uiState.update { it.copy(diagnostics = issues) }
        }
    }

    fun runCode() {
        val file = activeFile ?: return
        _uiState.update {
            it.copy(
                isRunning = true,
                currentTab = IdeTab.TERMINAL
            )
        }

        addTerminalLine("$ run ${file.name} --runtime=${file.language.id}", TerminalLineType.INPUT)

        viewModelScope.launch {
            val result = runtime.execute(
                language = file.language,
                code = file.content,
                activeEnvName = _uiState.value.activeEnvId
            )

            result.output.lines().forEach { line ->
                val type = if (result.success) TerminalLineType.OUTPUT else TerminalLineType.ERROR
                addTerminalLine(line, type)
            }

            val statusText = if (result.success) {
                "✔ Process finished in ${result.executionTimeMs}ms (RAM: ${String.format("%.1f", result.memoryUsageMb)} MB)"
            } else {
                "✖ Process exited with code ${result.exitCode} (${result.executionTimeMs}ms)"
            }
            addTerminalLine(statusText, if (result.success) TerminalLineType.SUCCESS else TerminalLineType.ERROR)

            _uiState.update {
                it.copy(
                    isRunning = false,
                    memoryUsageMb = result.memoryUsageMb
                )
            }
        }
    }

    fun sendTerminalCommand(command: String) {
        if (command.isBlank()) return
        addTerminalLine("$ $command", TerminalLineType.INPUT)
        _uiState.update { it.copy(terminalInput = "") }

        val trimmed = command.trim()
        when {
            trimmed == "clear" -> {
                _uiState.update { it.copy(terminalLines = emptyList()) }
            }
            trimmed == "help" -> {
                addTerminalLine("NovaCode Subsystem Commands:", TerminalLineType.SYSTEM)
                addTerminalLine("  run <file>         - Execute file using embedded compiler", TerminalLineType.OUTPUT)
                addTerminalLine("  python <args>      - Python 3.12 embedded interpreter", TerminalLineType.OUTPUT)
                addTerminalLine("  clang++ <args>     - Clang/LLVM C++ compiler", TerminalLineType.OUTPUT)
                addTerminalLine("  cargo run          - Compile & execute Rust workspace", TerminalLineType.OUTPUT)
                addTerminalLine("  pip install <pkg>  - Install package into virtualenv", TerminalLineType.OUTPUT)
                addTerminalLine("  env list           - List virtual environments", TerminalLineType.OUTPUT)
                addTerminalLine("  workspace list     - List available project workspaces", TerminalLineType.OUTPUT)
                addTerminalLine("  ls / uname         - Linux filesystem information", TerminalLineType.OUTPUT)
                addTerminalLine("  clear              - Clear terminal display", TerminalLineType.OUTPUT)
            }
            trimmed == "workspace list" -> {
                _uiState.value.workspaces.forEach { ws ->
                    val activeMarker = if (ws.id == _uiState.value.activeWorkspaceId) "* ACTIVE" else ""
                    addTerminalLine("${ws.name} (${ws.files.size} files) -> ${ws.rootPath} $activeMarker", TerminalLineType.OUTPUT)
                }
            }
            trimmed.startsWith("pip install ") -> {
                val pkg = trimmed.removePrefix("pip install ").trim()
                addTerminalLine("Collecting $pkg...", TerminalLineType.OUTPUT)
                viewModelScope.launch {
                    bootstrapManager.installPackage(_uiState.value.activeEnvId, pkg)
                    addTerminalLine("Successfully installed $pkg into current virtualenv.", TerminalLineType.SUCCESS)
                    _uiState.update { it.copy(environments = bootstrapManager.getEnvironments()) }
                }
            }
            trimmed == "env list" -> {
                _uiState.value.environments.forEach { env ->
                    val activeMarker = if (env.isActive) "* ACTIVE" else ""
                    addTerminalLine("${env.name} [${env.language.displayName}] $activeMarker", TerminalLineType.OUTPUT)
                }
            }
            trimmed == "ls" -> {
                val fileList = _uiState.value.openFiles.joinToString("   ") { it.name }
                addTerminalLine("src/   Cargo.toml   Makefile   requirements.txt   $fileList", TerminalLineType.OUTPUT)
            }
            trimmed.startsWith("uname") -> {
                addTerminalLine("Linux novacode 6.6.21-android #1 SMP PREEMPT aarch64 GNU/Linux", TerminalLineType.OUTPUT)
            }
            trimmed.startsWith("run ") -> {
                val fname = trimmed.removePrefix("run ").trim()
                val targetFile = _uiState.value.openFiles.find { it.name.equals(fname, ignoreCase = true) }
                if (targetFile != null) {
                    val idx = _uiState.value.openFiles.indexOf(targetFile)
                    selectFile(idx)
                    runCode()
                } else {
                    addTerminalLine("Error: File '$fname' not found in workspace", TerminalLineType.ERROR)
                }
            }
            else -> {
                addTerminalLine("novacode: exec: $trimmed -> dispatched to mobile Linux sandbox", TerminalLineType.OUTPUT)
            }
        }
    }

    private fun addTerminalLine(text: String, type: TerminalLineType) {
        val line = TerminalLine(text = text, type = type)
        _uiState.update { it.copy(terminalLines = it.terminalLines + line) }
    }

    fun performSearch(query: String, isRegex: Boolean) {
        _uiState.update { it.copy(searchQuery = query, isRegexSearch = isRegex) }
        viewModelScope.launch {
            val results = indexEngine.fullTextSearch(query, isRegex)
            _uiState.update { it.copy(searchResults = results) }
        }
    }

    fun jumpToSearchResult(result: ProjectIndexEngine.SearchResult) {
        val fileIdx = _uiState.value.openFiles.indexOfFirst { it.path == result.filePath }
        if (fileIdx != -1) {
            selectFile(fileIdx)
            _uiState.update {
                it.copy(
                    currentTab = IdeTab.EDITOR,
                    cursorLine = result.line,
                    cursorColumn = result.column
                )
            }
        }
    }

    fun sendAiPrompt(promptText: String) {
        if (promptText.isBlank()) return
        val userMsg = OpenCodeMessage(
            sender = MessageSender.USER,
            text = promptText
        )
        _uiState.update {
            it.copy(
                aiMessages = it.aiMessages + userMsg,
                isAiThinking = true
            )
        }

        viewModelScope.launch {
            val currentCode = activeFile?.content ?: ""
            val currentLang = activeFile?.language ?: SupportedLanguage.PYTHON
            val currentPath = activeFile?.path ?: "main.py"

            val aiResponse = aiService.sendQuery(
                prompt = promptText,
                currentCode = currentCode,
                currentLanguage = currentLang,
                filePath = currentPath
            )

            _uiState.update {
                it.copy(
                    aiMessages = it.aiMessages + aiResponse,
                    isAiThinking = false
                )
            }
        }
    }

    fun triggerVoiceOrb(start: Boolean) {
        _uiState.update {
            it.copy(
                isVoiceOrbActive = start,
                voiceOrbMode = if (start) VoiceOrbState.LISTENING else VoiceOrbState.IDLE,
                voiceTranscript = if (start) "Listening for developer command..." else ""
            )
        }
    }

    fun submitVoiceCommand(transcript: String) {
        _uiState.update {
            it.copy(
                voiceOrbMode = VoiceOrbState.PROCESSING,
                voiceTranscript = transcript
            )
        }
        viewModelScope.launch {
            sendAiPrompt(transcript)
            _uiState.update {
                it.copy(
                    voiceOrbMode = VoiceOrbState.SPEAKING
                )
            }
            kotlinx.coroutines.delay(1200)
            _uiState.update {
                it.copy(
                    voiceOrbMode = VoiceOrbState.IDLE,
                    isVoiceOrbActive = false
                )
            }
        }
    }

    fun createWorkspace(name: String, language: SupportedLanguage, description: String = "") {
        val slug = name.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
        val rootPath = "/workspaces/$slug"

        val initialFiles = when (language) {
            SupportedLanguage.PYTHON -> listOf(
                SourceFile(
                    name = "app.py",
                    path = "$rootPath/src/app.py",
                    language = SupportedLanguage.PYTHON,
                    content = "#!/usr/bin/env python3\n# Workspace: $name\nprint('Initializing $name workspace on Linux subsystem...')\n"
                ),
                SourceFile(
                    name = "requirements.txt",
                    path = "$rootPath/requirements.txt",
                    language = SupportedLanguage.SHELL,
                    content = "# Packages for $name\nnumpy>=1.26.0\nrequests>=2.31.0\n"
                )
            )
            SupportedLanguage.CPP -> listOf(
                SourceFile(
                    name = "main.cpp",
                    path = "$rootPath/src/main.cpp",
                    language = SupportedLanguage.CPP,
                    content = "// C++ Workspace: $name\n#include <iostream>\n\nint main() {\n    std::cout << \"Hello from $name C++20 engine!\" << std::endl;\n    return 0;\n}\n"
                ),
                SourceFile(
                    name = "Makefile",
                    path = "$rootPath/Makefile",
                    language = SupportedLanguage.SHELL,
                    content = "CXX = clang++\nCXXFLAGS = -std=c++20 -O3 -Wall\nall:\n\t\$(CXX) \$(CXXFLAGS) src/main.cpp -o app\n"
                )
            )
            SupportedLanguage.RUST -> listOf(
                SourceFile(
                    name = "main.rs",
                    path = "$rootPath/src/main.rs",
                    language = SupportedLanguage.RUST,
                    content = "// Rust Workspace: $name\nfn main() {\n    println!(\"Rust cargo project '$name' initialized.\");\n}\n"
                ),
                SourceFile(
                    name = "Cargo.toml",
                    path = "$rootPath/Cargo.toml",
                    language = SupportedLanguage.SHELL,
                    content = "[package]\nname = \"$slug\"\nversion = \"0.1.0\"\nedition = \"2021\"\n\n[dependencies]\n"
                )
            )
            SupportedLanguage.KOTLIN -> listOf(
                SourceFile(
                    name = "Main.kt",
                    path = "$rootPath/src/Main.kt",
                    language = SupportedLanguage.KOTLIN,
                    content = "// Kotlin Workspace: $name\nfun main() {\n    println(\"Kotlin 2.2 project '$name' initialized.\")\n}\n"
                )
            )
            SupportedLanguage.JAVA -> listOf(
                SourceFile(
                    name = "Main.java",
                    path = "$rootPath/src/Main.java",
                    language = SupportedLanguage.JAVA,
                    content = "// Java Workspace: $name\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Java 21 project '$name' initialized.\");\n    }\n}\n"
                )
            )
            SupportedLanguage.SHELL -> listOf(
                SourceFile(
                    name = "script.sh",
                    path = "$rootPath/src/script.sh",
                    language = SupportedLanguage.SHELL,
                    content = "#!/usr/bin/env bash\n# Shell Workspace: $name\necho \"Running $name deployment\"\n"
                )
            )
        }

        val newWorkspace = Workspace(
            id = "ws_${System.currentTimeMillis()}",
            name = name,
            rootPath = rootPath,
            description = description.ifBlank { "Linux workspace for ${language.displayName}" },
            files = initialFiles,
            activeFileIndex = 0,
            defaultLanguage = language
        )

        val updatedList = _uiState.value.workspaces + newWorkspace
        _uiState.update {
            it.copy(
                workspaces = updatedList,
                activeWorkspaceId = newWorkspace.id,
                openFiles = initialFiles,
                activeFileIndex = 0,
                totalLines = initialFiles[0].content.lines().size
            )
        }

        viewModelScope.launch {
            indexEngine.reindexAll(initialFiles)
            updateDiagnostics()
        }

        addTerminalLine("Created and mounted new workspace '${newWorkspace.name}' at $rootPath", TerminalLineType.SUCCESS)
    }

    fun switchWorkspace(workspaceId: String) {
        val target = _uiState.value.workspaces.find { it.id == workspaceId } ?: return

        _uiState.update {
            it.copy(
                activeWorkspaceId = workspaceId,
                openFiles = target.files,
                activeFileIndex = target.activeFileIndex.coerceIn(0, (target.files.size - 1).coerceAtLeast(0)),
                totalLines = target.files.getOrNull(target.activeFileIndex)?.content?.lines()?.size ?: 1
            )
        }

        viewModelScope.launch {
            indexEngine.reindexAll(target.files)
            updateDiagnostics()
        }

        addTerminalLine("Switched to workspace '${target.name}' (${target.rootPath})", TerminalLineType.SYSTEM)
    }

    fun createVirtualEnv(name: String, language: SupportedLanguage) {
        viewModelScope.launch {
            val newEnv = bootstrapManager.createEnvironment(name, language)
            _uiState.update { it.copy(environments = bootstrapManager.getEnvironments()) }
            addTerminalLine("Created virtual environment '${newEnv.name}' for ${language.displayName}", TerminalLineType.SUCCESS)
        }
    }

    fun activateVirtualEnv(envId: String) {
        viewModelScope.launch {
            bootstrapManager.activateEnvironment(envId)
            _uiState.update {
                it.copy(
                    environments = bootstrapManager.getEnvironments(),
                    activeEnvId = envId
                )
            }
            val activeName = _uiState.value.environments.find { it.id == envId }?.name ?: envId
            addTerminalLine("Switched active toolchain to '$activeName'", TerminalLineType.SYSTEM)
        }
    }

    fun installPackageToEnv(envId: String, packageName: String) {
        viewModelScope.launch {
            val success = bootstrapManager.installPackage(envId, packageName)
            if (success) {
                _uiState.update { it.copy(environments = bootstrapManager.getEnvironments()) }
                addTerminalLine("Installed $packageName into $envId", TerminalLineType.SUCCESS)
            }
        }
    }

    fun addNewFile(name: String, language: SupportedLanguage) {
        val root = activeWorkspace?.rootPath ?: "/workspace"
        val newFile = SourceFile(
            name = name,
            path = "$root/src/$name",
            language = language,
            content = when (language) {
                SupportedLanguage.PYTHON -> "# Python script\nprint('Hello from $name')\n"
                SupportedLanguage.CPP -> "// C++ Source\n#include <iostream>\n\nint main() {\n    std::cout << \"Running $name\" << std::endl;\n    return 0;\n}\n"
                SupportedLanguage.RUST -> "// Rust Source\nfn main() {\n    println!(\"Running $name\");\n}\n"
                SupportedLanguage.KOTLIN -> "// Kotlin Source\nfun main() {\n    println(\"Running $name\")\n}\n"
                SupportedLanguage.JAVA -> "// Java Source\npublic class ${name.removeSuffix(".java")} {\n    public static void main(String[] args) {\n        System.out.println(\"Running $name\");\n    }\n}\n"
                SupportedLanguage.SHELL -> "#!/usr/bin/env bash\necho \"Running $name\"\n"
            }
        )
        val files = _uiState.value.openFiles + newFile
        val currentWsId = _uiState.value.activeWorkspaceId
        val updatedWorkspaces = _uiState.value.workspaces.map { ws ->
            if (ws.id == currentWsId) ws.copy(files = files, activeFileIndex = files.size - 1) else ws
        }

        _uiState.update {
            it.copy(
                openFiles = files,
                workspaces = updatedWorkspaces,
                activeFileIndex = files.size - 1
            )
        }
        viewModelScope.launch {
            indexEngine.indexFile(newFile)
        }
    }

    fun closeFile(index: Int) {
        if (_uiState.value.openFiles.size <= 1) return
        val currentFiles = _uiState.value.openFiles.toMutableList()
        currentFiles.removeAt(index)
        val newIdx = index.coerceAtMost(currentFiles.size - 1)
        val currentWsId = _uiState.value.activeWorkspaceId
        val updatedWorkspaces = _uiState.value.workspaces.map { ws ->
            if (ws.id == currentWsId) ws.copy(files = currentFiles, activeFileIndex = newIdx) else ws
        }

        _uiState.update {
            it.copy(
                openFiles = currentFiles,
                workspaces = updatedWorkspaces,
                activeFileIndex = newIdx
            )
        }
    }
}
