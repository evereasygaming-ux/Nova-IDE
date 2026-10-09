package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.model.SupportedLanguage
import com.example.ui.components.*
import com.example.viewmodel.IdeTab
import com.example.viewmodel.IdeViewModel

@Composable
fun NovaCodeIdeMainScreen(
    viewModel: IdeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var newFileLanguage by remember { mutableStateOf(SupportedLanguage.PYTHON) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ProjectFilesTreeDrawer(
                workspaces = uiState.workspaces,
                activeWorkspaceId = uiState.activeWorkspaceId,
                openFiles = uiState.openFiles,
                activeFileIndex = uiState.activeFileIndex,
                onSelectWorkspace = { wsId ->
                    viewModel.switchWorkspace(wsId)
                },
                onCreateWorkspace = { name, lang ->
                    viewModel.createWorkspace(name, lang)
                },
                onSelectFile = { index ->
                    viewModel.selectFile(index)
                    viewModel.selectTab(IdeTab.EDITOR)
                },
                onAddNewFile = { showNewFileDialog = true },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .imePadding(),
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = Color(0xFF090D16),
            topBar = {
                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                    IdeTopAppBar(
                        activeFile = viewModel.activeFile,
                        isRunning = uiState.isRunning,
                        fps = uiState.fpsMeter,
                        ramMb = uiState.memoryUsageMb,
                        onRunCode = { viewModel.runCode() },
                        onOpenVoiceOrb = { viewModel.triggerVoiceOrb(true) },
                        onNewFileClick = { showNewFileDialog = true },
                        onToggleDrawer = {
                            coroutineScope.launch {
                                if (drawerState.isOpen) drawerState.close() else drawerState.open()
                            }
                        }
                    )

                    if (uiState.currentTab == IdeTab.EDITOR) {
                        IdeTabBar(
                            openFiles = uiState.openFiles,
                            activeFileIndex = uiState.activeFileIndex,
                            onSelectFile = { viewModel.selectFile(it) },
                            onCloseFile = { viewModel.closeFile(it) },
                            onAddNewFile = { showNewFileDialog = true }
                        )
                    }
                }
            },
            bottomBar = {
                IdeBottomNavBar(
                    currentTab = uiState.currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                IdeTab.EDITOR -> {
                    AnimatedContent(
                        targetState = uiState.activeFileIndex,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith
                                fadeOut(animationSpec = tween(150))
                        },
                        label = "file_switch_transition"
                    ) { targetIndex ->
                        val activeFile = uiState.openFiles.getOrNull(targetIndex)
                        if (activeFile != null) {
                            FuturisticCodeEditor(
                                file = activeFile,
                                diagnostics = uiState.diagnostics,
                                completionItems = uiState.completionSuggestions,
                                onCodeChange = { viewModel.updateCode(it) },
                                onCursorMove = { line, col, prefix ->
                                    viewModel.updateCursor(line, col, prefix)
                                },
                                onSwipeNextFile = {
                                    val nextIdx = (uiState.activeFileIndex + 1) % uiState.openFiles.size
                                    viewModel.selectFile(nextIdx)
                                },
                                onSwipePrevFile = {
                                    val prevIdx = if (uiState.activeFileIndex - 1 < 0) uiState.openFiles.size - 1 else uiState.activeFileIndex - 1
                                    viewModel.selectFile(prevIdx)
                                },
                                onFastScrollToLine = { targetLine ->
                                    viewModel.updateCursor(targetLine, 1, "")
                                }
                            )
                        }
                    }
                }
                IdeTab.TERMINAL -> {
                    RealtimeTerminalView(
                        terminalLines = uiState.terminalLines,
                        isRunning = uiState.isRunning,
                        onSendCommand = { viewModel.sendTerminalCommand(it) },
                        onClear = { viewModel.sendTerminalCommand("clear") }
                    )
                }
                IdeTab.ENVIRONMENTS -> {
                    VirtualEnvironmentsManagerView(
                        environments = uiState.environments,
                        activeEnvId = uiState.activeEnvId,
                        onActivateEnv = { viewModel.activateVirtualEnv(it) },
                        onCreateEnv = { name, lang -> viewModel.createVirtualEnv(name, lang) },
                        onInstallPackage = { envId, pkg -> viewModel.installPackageToEnv(envId, pkg) }
                    )
                }
                IdeTab.SEARCH_INDEX -> {
                    LightningSearchAndIndexView(
                        searchQuery = uiState.searchQuery,
                        searchResults = uiState.searchResults,
                        isRegex = uiState.isRegexSearch,
                        onSearch = { query, regex -> viewModel.performSearch(query, regex) },
                        onJumpToResult = { viewModel.jumpToSearchResult(it) }
                    )
                }
                IdeTab.OPENCODE_AI -> {
                    OpenCodeAiChatView(
                        messages = uiState.aiMessages,
                        isThinking = uiState.isAiThinking,
                        onSendPrompt = { viewModel.sendAiPrompt(it) },
                        onOpenVoiceOrb = { viewModel.triggerVoiceOrb(true) }
                    )
                }
            }

            // 3D Voice Orb Holographic Overlay Dialog
            AnimatedVisibility(
                visible = uiState.isVoiceOrbActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Holographic3DVoiceOrb(
                    state = uiState.voiceOrbMode,
                    transcript = uiState.voiceTranscript,
                    onDismiss = { viewModel.triggerVoiceOrb(false) },
                    onSubmitCommand = { viewModel.submitVoiceCommand(it) }
                )
            }
        }
    }
}

    // New File Creation Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create Source File", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        label = { Text("Filename") },
                        placeholder = { Text("e.g. algorithm.cpp or solver.rs") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Select Language:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SupportedLanguage.values().forEach { lang ->
                            FilterChip(
                                selected = newFileLanguage == lang,
                                onClick = {
                                    newFileLanguage = lang
                                    if (newFileName.isBlank() || newFileName.startsWith("untitled")) {
                                        newFileName = "script${lang.extension}"
                                    }
                                },
                                label = { Text(lang.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newFileName.isNotBlank()) {
                        viewModel.addNewFile(newFileName, newFileLanguage)
                        newFileName = ""
                        showNewFileDialog = false
                    }
                }) {
                    Text("Create File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
