package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun IdeTopAppBar(
    activeFile: SourceFile?,
    isRunning: Boolean,
    fps: Int,
    ramMb: Double,
    onRunCode: () -> Unit,
    onOpenVoiceOrb: () -> Unit,
    onNewFileClick: () -> Unit,
    onToggleDrawer: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF090D16),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Navigation Drawer Toggle & Brand Title with Telemetry HUD
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onToggleDrawer != null) {
                        IconButton(
                            onClick = onToggleDrawer,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("topbar_drawer_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Project Files Tree Drawer",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NOVACODE",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0x3300FFC2)
                            ) {
                                Text(
                                    text = "LINUX IDE",
                                    color = Color(0xFF00FFC2),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        // Live 60 FPS Engine Counter & RAM
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$fps FPS  |  ${String.format("%.1f", ramMb)} MB RAM",
                                color = Color(0xFF00FFC2),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Action controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Voice 3D Orb Shortcut Button
                    FilledIconButton(
                        onClick = onOpenVoiceOrb,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("topbar_voice_orb_button"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF131D33),
                            contentColor = Color(0xFF00E5FF)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Assistant Orb",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Run / Stop compiler action
                    Button(
                        onClick = onRunCode,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) Color(0xFFFF5370) else Color(0xFF00E5FF),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("topbar_run_code_button")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Stop Execution" else "Run Code",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRunning) "STOP" else "RUN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Bottom subtle divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF1E293B))
            )
        }
    }
}
