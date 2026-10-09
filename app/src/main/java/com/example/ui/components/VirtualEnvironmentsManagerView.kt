package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.SupportedLanguage
import com.example.model.VirtualEnvironment

@Composable
fun VirtualEnvironmentsManagerView(
    environments: List<VirtualEnvironment>,
    activeEnvId: String,
    onActivateEnv: (String) -> Unit,
    onCreateEnv: (name: String, language: SupportedLanguage) -> Unit,
    onInstallPackage: (envId: String, pkgName: String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showInstallDialog by remember { mutableStateOf(false) }
    var selectedEnvForInstall by remember { mutableStateOf<String?>(null) }

    var newEnvName by remember { mutableStateOf("") }
    var selectedLang by remember { mutableStateOf(SupportedLanguage.PYTHON) }
    var newPkgName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TOOLCHAINS & VIRTUAL ENVS",
                    color = Color(0xFF00E5FF),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Isolated runtime packages for Python, LLVM/Clang & Rust",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("create_env_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Env", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Env", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(environments, key = { it.id }) { env ->
                val isActive = env.id == activeEnvId

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) Color(0xFF131D33) else Color(0xFF0F172A)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isActive) Color(0xFF00E5FF) else Color(0xFF1E293B)
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                        .background(if (isActive) Color(0xFF00FFC2) else Color(0xFF475569))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = env.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (!isActive) {
                                OutlinedButton(
                                    onClick = { onActivateEnv(env.id) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF38BDF8)
                                    ),
                                    modifier = Modifier.testTag("activate_env_${env.id}")
                                ) {
                                    Text("Activate", fontSize = 11.sp)
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0x3300FFC2)
                                ) {
                                    Text(
                                        text = "ACTIVE RUNTIME",
                                        color = Color(0xFF00FFC2),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Path: ${env.path}",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Language Engine: ${env.language.compilerName}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Installed Packages
                        Text(
                            text = "INSTALLED PACKAGES (${env.packages.size}):",
                            color = Color(0xFF475569),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            env.packages.forEach { pkg ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0B101D), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${pkg.name} == ${pkg.version}",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = pkg.size,
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    selectedEnvForInstall = env.id
                                    showInstallDialog = true
                                },
                                modifier = Modifier.testTag("install_pkg_btn_${env.id}")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Install Package/Crate", fontSize = 12.sp, color = Color(0xFF00E5FF))
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Env Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Bootstrap New Environment", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newEnvName,
                        onValueChange = { newEnvName = it },
                        label = { Text("Environment Name") },
                        placeholder = { Text("e.g. data-science-py312") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Target Runtime Toolchain:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SupportedLanguage.values().forEach { lang ->
                            FilterChip(
                                selected = selectedLang == lang,
                                onClick = { selectedLang = lang },
                                label = { Text(lang.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newEnvName.isNotBlank()) {
                        onCreateEnv(newEnvName, selectedLang)
                        newEnvName = ""
                        showCreateDialog = false
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Install Package Dialog
    if (showInstallDialog && selectedEnvForInstall != null) {
        AlertDialog(
            onDismissRequest = { showInstallDialog = false },
            title = { Text("Install Dependency into Sandbox", color = Color.White) },
            text = {
                Column {
                    Text("Package or Crate Name:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newPkgName,
                        onValueChange = { newPkgName = it },
                        placeholder = { Text("e.g. scipy, tokio, fmt") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newPkgName.isNotBlank()) {
                        onInstallPackage(selectedEnvForInstall!!, newPkgName)
                        newPkgName = ""
                        showInstallDialog = false
                    }
                }) {
                    Text("Install")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstallDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
