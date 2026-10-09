package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
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
import com.example.model.MessageSender
import com.example.model.OpenCodeMessage

@Composable
fun OpenCodeAiChatView(
    messages: List<OpenCodeMessage>,
    isThinking: Boolean,
    onSendPrompt: (String) -> Unit,
    onOpenVoiceOrb: () -> Unit
) {
    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
    ) {
        // AI Header HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF007F))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "OPENCODE AI ASSISTANT",
                    color = Color(0xFFFF007F),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            FilledTonalButton(
                onClick = onOpenVoiceOrb,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0x3300E5FF),
                    contentColor = Color(0xFF00E5FF)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("launch_voice_orb_chat_button")
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Orb", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("3D Voice Orb", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Messages Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.sender == MessageSender.USER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) Color(0xFF1E293B) else Color(0xFF131D33)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isUser) Color(0xFF38BDF8) else Color(0xFFFF007F)
                            )
                        ),
                        modifier = Modifier.fillMaxWidth(0.88f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isUser) Icons.Default.Send else Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isUser) Color(0xFF38BDF8) else Color(0xFFFF007F),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isUser) "Developer" else "OpenCode AI",
                                    color = if (isUser) Color(0xFF38BDF8) else Color(0xFFFF007F),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = msg.text,
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D33)),
                            modifier = Modifier.fillMaxWidth(0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFFFF007F),
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Analyzing codebase AST...",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Prompt Suggestions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Optimize code", "Explain architecture", "Check memory safety").forEach { prompt ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.testTag("ai_quick_prompt_$prompt")
                ) {
                    TextButton(
                        onClick = { onSendPrompt(prompt) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = prompt, color = Color(0xFF38BDF8), fontSize = 11.sp)
                    }
                }
            }
        }

        // Prompt Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = { Text("Ask OpenCode AI anything...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("opencode_ai_input_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF007F),
                    unfocusedBorderColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledIconButton(
                onClick = {
                    if (inputPrompt.isNotBlank()) {
                        onSendPrompt(inputPrompt)
                        inputPrompt = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("opencode_ai_send_button"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFFFF007F),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send AI Prompt", modifier = Modifier.size(18.dp))
            }
        }
    }
}
