package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TerminalLine
import com.example.model.TerminalLineType

@Composable
fun RealtimeTerminalView(
    terminalLines: List<TerminalLine>,
    isRunning: Boolean,
    onSendCommand: (String) -> Unit,
    onClear: () -> Unit
) {
    var inputCommand by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom whenever new lines appear
    LaunchedEffect(terminalLines.size) {
        if (terminalLines.isNotEmpty()) {
            listState.animateScrollToItem(terminalLines.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
    ) {
        // Terminal Top Status Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isRunning) Color(0xFFFF9900) else Color(0xFF00FFC2))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "EMBEDDED PROCESS EXECUTING..." else "LINUX SUBSYSTEM ONLINE [TTY 1]",
                    color = if (isRunning) Color(0xFFFFCC00) else Color(0xFF00FFC2),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(28.dp).testTag("terminal_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = "Clear Terminal",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Terminal Log Console
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(terminalLines, key = { it.id }) { line ->
                val textColor = when (line.type) {
                    TerminalLineType.INPUT -> Color(0xFF00E5FF)
                    TerminalLineType.OUTPUT -> Color(0xFFCBD5E1)
                    TerminalLineType.SUCCESS -> Color(0xFF00FFC2)
                    TerminalLineType.ERROR -> Color(0xFFFF5370)
                    TerminalLineType.SYSTEM -> Color(0xFF7DD3FC)
                    TerminalLineType.AI_STREAM -> Color(0xFFFF007F)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = line.text,
                        color = textColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Quick Command Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B101D))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("help", "clear", "ls", "env list", "uname -a").forEach { cmd ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.clickable {
                        onSendCommand(cmd)
                    }
                ) {
                    Text(
                        text = cmd,
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Terminal Prompt Input Line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "root@novacode:~$ ",
                color = Color(0xFF00E5FF),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            TextField(
                value = inputCommand,
                onValueChange = { inputCommand = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_input_field"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                ),
                placeholder = {
                    Text(
                        text = "enter command (e.g. run main.py, pip install)",
                        color = Color(0xFF475569),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputCommand.isNotBlank()) {
                        onSendCommand(inputCommand)
                        inputCommand = ""
                    }
                })
            )

            FilledIconButton(
                onClick = {
                    if (inputCommand.isNotBlank()) {
                        onSendCommand(inputCommand)
                        inputCommand = ""
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("terminal_send_button"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Terminal Command",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
