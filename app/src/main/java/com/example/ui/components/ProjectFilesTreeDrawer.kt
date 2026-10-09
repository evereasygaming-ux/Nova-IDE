package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SourceFile
import com.example.model.SupportedLanguage
import com.example.model.Workspace

/**
 * Tree-view project files navigation drawer content with Multi-Workspace support.
 * Displays workspaces switcher, directory structure (workspace, src/, scripts, configs)
 * and active status for switching files open in the CodeEditor.
 */
@Composable
fun ProjectFilesTreeDrawer(
    workspaces: List<Workspace>,
    activeWorkspaceId: String,
    openFiles: List<SourceFile>,
    activeFileIndex: Int,
    onSelectWorkspace: (String) -> Unit,
    onCreateWorkspace: (name: String, language: SupportedLanguage) -> Unit,
    onSelectFile: (Int) -> Unit,
    onAddNewFile: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    var isSrcFolderExpanded by remember { mutableStateOf(true) }
    var isRootFolderExpanded by remember { mutableStateOf(true) }
    var showWorkspaceMenu by remember { mutableStateOf(false) }
    var showNewWorkspaceDialog by remember { mutableStateOf(false) }

    var newWsName by remember { mutableStateOf("") }
    var newWsLanguage by remember { mutableStateOf(SupportedLanguage.PYTHON) }

    val currentWorkspace = remember(workspaces, activeWorkspaceId) {
        workspaces.find { it.id == activeWorkspaceId } ?: workspaces.firstOrNull()
    }

    ModalDrawerSheet(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("project_files_tree_drawer"),
        drawerContainerColor = Color(0xFF070B14),
        drawerContentColor = Color(0xFFE2E8F0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Drawer Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PROJECT EXPLORER",
                        color = Color(0xFF00E5FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onAddNewFile,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("drawer_add_file_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Source File",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Workspace Selector Card / Switcher
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F172A),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showWorkspaceMenu = !showWorkspaceMenu }
                    .testTag("workspace_selector_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Source,
                            contentDescription = null,
                            tint = Color(0xFF00FFC2),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = currentWorkspace?.name ?: "Workspace",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = currentWorkspace?.rootPath ?: "/workspace",
                                color = Color(0xFF64748B),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Icon(
                        imageVector = if (showWorkspaceMenu) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Workspace Menu",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Dropdown list of workspaces
            AnimatedVisibility(visible = showWorkspaceMenu) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .background(Color(0xFF0B101D), RoundedCornerShape(8.dp))
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "SWITCH WORKSPACE",
                        color = Color(0xFF475569),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    workspaces.forEach { ws ->
                        val isCurrent = ws.id == activeWorkspaceId
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCurrent) Color(0xFF131D33) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectWorkspace(ws.id)
                                    showWorkspaceMenu = false
                                }
                                .testTag("workspace_item_${ws.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ws.name,
                                    color = if (isCurrent) Color(0xFF00E5FF) else Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (isCurrent) {
                                    Text(text = "ACTIVE", color = Color(0xFF00FFC2), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Button to Add a New Workspace
                    Button(
                        onClick = {
                            showWorkspaceMenu = false
                            showNewWorkspaceDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x3300E5FF),
                            contentColor = Color(0xFF00E5FF)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drawer_add_workspace_btn"),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Workspace", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))

            // Tree View File List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Root Workspace Folder
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isRootFolderExpanded = !isRootFolderExpanded }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isRootFolderExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFFFFCB6B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentWorkspace?.name ?: "workspace",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${openFiles.size} files)",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (isRootFolderExpanded) {
                    // Nested src/ directory node
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp)
                                .clickable { isSrcFolderExpanded = !isSrcFolderExpanded }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSrcFolderExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "src/",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Files under src/
                    if (isSrcFolderExpanded) {
                        itemsIndexed(openFiles) { index, file ->
                            val isActive = index == activeFileIndex
                            val langColor = when (file.language) {
                                SupportedLanguage.PYTHON -> Color(0xFF3572A5)
                                SupportedLanguage.CPP -> Color(0xFFF34B7D)
                                SupportedLanguage.RUST -> Color(0xFFDEA584)
                                SupportedLanguage.KOTLIN -> Color(0xFF7F52FF)
                                SupportedLanguage.JAVA -> Color(0xFFB07219)
                                SupportedLanguage.SHELL -> Color(0xFF89E051)
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 36.dp)
                                    .clickable {
                                        onSelectFile(index)
                                        onCloseDrawer()
                                    }
                                    .testTag("drawer_file_item_${file.name}"),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isActive) Color(0xFF131D33) else Color.Transparent,
                                border = if (isActive) CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF00E5FF))
                                ) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(langColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = file.name,
                                            color = if (isActive) Color(0xFF00E5FF) else Color(0xFFCBD5E1),
                                            fontSize = 12.sp,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    if (isActive) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0x3300FFC2)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color(0xFF00FFC2),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    } else if (file.isModified) {
                                        Text(text = "●", color = Color(0xFFFF9900), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Static Config Nodes in Workspace
                    item {
                        Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cargo.toml", color = Color(0xFF64748B), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Makefile", color = Color(0xFF64748B), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("requirements.txt", color = Color(0xFF64748B), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            // Footer Quick Action Buttons
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        showNewWorkspaceDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("drawer_add_ws_footer_btn"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF38BDF8)
                    )
                ) {
                    Text("+ Workspace", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        onAddNewFile()
                        onCloseDrawer()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("drawer_create_file_footer_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+ New File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Dialog to Add a Workspace
    if (showNewWorkspaceDialog) {
        AlertDialog(
            onDismissRequest = { showNewWorkspaceDialog = false },
            title = { Text("Add Project Workspace", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newWsName,
                        onValueChange = { newWsName = it },
                        label = { Text("Workspace Name") },
                        placeholder = { Text("e.g. neural-engine or raytracer") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Primary Language Toolchain:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SupportedLanguage.values().forEach { lang ->
                            FilterChip(
                                selected = newWsLanguage == lang,
                                onClick = { newWsLanguage = lang },
                                label = { Text(lang.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newWsName.isNotBlank()) {
                        onCreateWorkspace(newWsName, newWsLanguage)
                        newWsName = ""
                        showNewWorkspaceDialog = false
                    }
                }) {
                    Text("Create Workspace")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewWorkspaceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
