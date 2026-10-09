package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.viewmodel.VoiceOrbState
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-tech Futuristic 3D Holographic Orb Menu with real-time particle rotation,
 * pulsing soundwave resonance, and hands-free developer voice control.
 */
@Composable
fun Holographic3DVoiceOrb(
    state: VoiceOrbState,
    transcript: String,
    onDismiss: () -> Unit,
    onSubmitCommand: (String) -> Unit
) {
    var userVoiceInput by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "orb_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6050811))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* prevent dismiss when clicking dialog body */ }
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0D1322)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF00E5FF), Color(0xFFFF007F), Color(0xFF7000FF))
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar
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
                                text = "OPENCODE 3D VOICE ORB",
                                color = Color(0xFF00E5FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("close_voice_orb_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Voice Orb",
                                tint = Color(0xFF8892B0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3D Canvas Orb
                    Box(
                        modifier = Modifier
                            .size(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val baseRadius = (size.minDimension / 2.6f) * pulseScale

                            // Outer Neon Glow Radial
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x6600E5FF),
                                        Color(0x33FF007F),
                                        Color(0x00000000)
                                    ),
                                    center = Offset(centerX, centerY),
                                    radius = baseRadius * 1.5f
                                ),
                                radius = baseRadius * 1.5f,
                                center = Offset(centerX, centerY)
                            )

                            // 3D Orbital Rings tilted in perspective
                            val ringCount = 5
                            for (i in 0 until ringCount) {
                                val radOffset = (i * 360f / ringCount)
                                val currentAngleRad = Math.toRadians((rotationAngle + radOffset).toDouble())
                                val ringTilt = 0.45f + (i * 0.12f)
                                
                                val ringColor = if (i % 2 == 0) Color(0xFF00E5FF) else Color(0xFFFF007F)

                                drawCircle(
                                    color = ringColor.copy(alpha = glowAlpha * 0.7f),
                                    radius = baseRadius * (0.6f + i * 0.1f),
                                    center = Offset(
                                        centerX + (cos(currentAngleRad) * 12f).toFloat(),
                                        centerY + (sin(currentAngleRad) * 8f).toFloat()
                                    ),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }

                            // Rotating Orbital Energy Nodes
                            val nodesCount = 12
                            for (k in 0 until nodesCount) {
                                val nodeAngle = Math.toRadians((rotationAngle * 1.5 + (k * 360.0 / nodesCount)))
                                val nx = centerX + (baseRadius * cos(nodeAngle)).toFloat()
                                val ny = centerY + (baseRadius * 0.75f * sin(nodeAngle)).toFloat()

                                drawCircle(
                                    color = if (k % 3 == 0) Color(0xFFFFFFFF) else Color(0xFF00FFC2),
                                    radius = 4.dp.toPx(),
                                    center = Offset(nx, ny)
                                )
                            }

                            // Inner Quantum Core
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFFFFF),
                                        Color(0xFF00E5FF),
                                        Color(0xFF7000FF)
                                    ),
                                    center = Offset(centerX, centerY),
                                    radius = baseRadius * 0.5f
                                ),
                                radius = baseRadius * 0.5f,
                                center = Offset(centerX, centerY)
                            )
                        }

                        // Status Icon in Core
                        Icon(
                            imageVector = when (state) {
                                VoiceOrbState.LISTENING -> Icons.Default.Mic
                                VoiceOrbState.PROCESSING -> Icons.Default.GraphicEq
                                VoiceOrbState.SPEAKING -> Icons.Default.VolumeUp
                                else -> Icons.Default.Mic
                            },
                            contentDescription = "Orb State Icon",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Orb State Label
                    Text(
                        text = when (state) {
                            VoiceOrbState.LISTENING -> "● LISTENING TO SPEECH..."
                            VoiceOrbState.PROCESSING -> "⚡ NEURAL SYNTHESIS IN PROGRESS..."
                            VoiceOrbState.SPEAKING -> "🔊 EXECUTING OPENCODE RESPONSE..."
                            VoiceOrbState.IDLE -> "READY FOR VOICE COMMAND"
                        },
                        color = when (state) {
                            VoiceOrbState.LISTENING -> Color(0xFF00FFC2)
                            VoiceOrbState.PROCESSING -> Color(0xFFFFCC00)
                            VoiceOrbState.SPEAKING -> Color(0xFFFF007F)
                            VoiceOrbState.IDLE -> Color(0xFF8892B0)
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (transcript.isNotEmpty()) "\"$transcript\"" else "Try saying: \"Optimize nested loops in main.py\" or \"Compile and run C++ vector\"",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Quick Command Chips
                    Text(
                        text = "QUICK VOICE INTENTS",
                        color = Color(0xFF5A6987),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = {
                                onSubmitCommand("Optimize active file performance")
                            },
                            label = { Text("⚡ Optimize", fontSize = 12.sp, color = Color(0xFF00E5FF)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0x2200E5FF)
                            )
                        )
                        SuggestionChip(
                            onClick = {
                                onSubmitCommand("Find syntax errors & borrow check")
                            },
                            label = { Text("🛠️ Debug", fontSize = 12.sp, color = Color(0xFFFF007F)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0x22FF007F)
                            )
                        )
                        SuggestionChip(
                            onClick = {
                                onSubmitCommand("Compile and run project")
                            },
                            label = { Text("▶ Run", fontSize = 12.sp, color = Color(0xFF00FFC2)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0x2200FFC2)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Manual Text Prompt Input (in case mic has no audio input in emulator)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userVoiceInput,
                            onValueChange = { userVoiceInput = it },
                            placeholder = { Text("Speak or type command...", color = Color(0xFF5A6987), fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("voice_orb_input_field"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledIconButton(
                            onClick = {
                                if (userVoiceInput.isNotBlank()) {
                                    onSubmitCommand(userVoiceInput)
                                    userVoiceInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("voice_orb_send_button"),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send Voice Command")
                        }
                    }
                }
            }
        }
    }
}
