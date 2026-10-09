package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SyntaxHighlighter
import com.example.model.CompletionItem
import com.example.model.DiagnosticIssue
import com.example.model.DiagnosticSeverity
import com.example.model.SourceFile

@Composable
fun FuturisticCodeEditor(
    file: SourceFile,
    diagnostics: List<DiagnosticIssue>,
    completionItems: List<CompletionItem>,
    onCodeChange: (String) -> Unit,
    onCursorMove: (line: Int, column: Int, prefix: String) -> Unit,
    onSwipeNextFile: () -> Unit,
    onSwipePrevFile: () -> Unit,
    onFastScrollToLine: (Int) -> Unit
) {
    var textContent by remember(file.path) { mutableStateOf(file.content) }
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    // Gesture navigation detector
    var totalDragX by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .pointerInput(file.path) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDragX = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDragX += dragAmount
                    },
                    onDragEnd = {
                        if (totalDragX > 150f) {
                            onSwipePrevFile()
                        } else if (totalDragX < -150f) {
                            onSwipeNextFile()
                        }
                        totalDragX = 0f
                    }
                )
            }
    ) {
        // Quick Gesture Navigation Strip & Status HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (file.language.id) {
                        "python" -> Color(0xFF3572A5)
                        "cpp" -> Color(0xFFF34B7D)
                        "rust" -> Color(0xFFDEA584)
                        else -> Color(0xFF89E051)
                    }
                ) {
                    Text(
                        text = file.language.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${file.name} ${if (file.isModified) "●" else ""}",
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }

            // Gesture Navigator Indicators
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onSwipePrevFile,
                    modifier = Modifier.size(28.dp).testTag("gesture_prev_file")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Swipe Left Prev File",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "SWIPE TABS",
                    color = Color(0xFF475569),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                IconButton(
                    onClick = onSwipeNextFile,
                    modifier = Modifier.size(28.dp).testTag("gesture_next_file")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Swipe Right Next File",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Predictive Autocomplete Bar (Sub-millisecond suggestions)
        if (completionItems.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131D33))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(completionItems) { item ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier
                            .clickable {
                                // Insert completion item
                                val updated = if (textContent.endsWith(item.label.take(1))) {
                                    textContent + item.insertText.drop(1)
                                } else {
                                    textContent + item.insertText
                                }
                                textContent = updated
                                onCodeChange(updated)
                            }
                            .testTag("completion_chip_${item.label}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.label,
                                color = Color(0xFF00E5FF),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.detail,
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Diagnostics HUD banner if errors present
        val errorDiagnostics = diagnostics.filter { it.severity == DiagnosticSeverity.ERROR }
        if (errorDiagnostics.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x33FF0055))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Diagnostics Error",
                    tint = Color(0xFFFF0055),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Line ${errorDiagnostics.first().line}: ${errorDiagnostics.first().message}",
                    color = Color(0xFFFF5370),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }

        // Editor Workspace Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScroll)
            ) {
                // Line Numbers Gutter
                val lines = textContent.lines()
                Column(
                    modifier = Modifier
                        .background(Color(0xFF0B0F19))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    lines.forEachIndexed { index, _ ->
                        val lineNum = index + 1
                        val hasIssue = diagnostics.any { it.line == lineNum }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (hasIssue) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF0055))
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                            Text(
                                text = "$lineNum",
                                color = if (hasIssue) Color(0xFFFF5370) else Color(0xFF334155),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF1E293B))
                )

                // Editable Code Text Area with Syntax Highlighting Overlay
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScroll)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Syntax Highlighting Render
                    val annotatedCode = remember(textContent, file.language) {
                        SyntaxHighlighter.highlight(textContent, file.language)
                    }

                    // Background syntax colored text
                    Text(
                        text = annotatedCode,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Color.Transparent
                    )

                    // Foreground live interactive editor
                    BasicTextField(
                        value = textContent,
                        onValueChange = { newText ->
                            val processed = com.example.engine.AutoIndentationEngine.processTextChange(
                                oldText = textContent,
                                newText = newText,
                                language = file.language
                            )
                            textContent = processed
                            onCodeChange(processed)
                            // calculate prefix for completions
                            val lastWord = processed.takeLastWhile { it.isLetterOrDigit() || it == '_' || it == ':' }
                            val lineCount = processed.lines().size
                            onCursorMove(lineCount, lastWord.length, lastWord)
                        },
                        textStyle = TextStyle(
                            color = Color(0xFFE2E8F0),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF00E5FF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("code_editor_text_field")
                    )
                }
            }

            // Quick Floating MiniMap Line Jumper (Right Side Gesture Slider)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(24.dp)
                    .fillMaxHeight()
                    .background(Color(0x221E293B))
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val fraction = (change.position.y / size.height).coerceIn(0f, 1f)
                            val targetLine = (fraction * textContent.lines().size).toInt().coerceAtLeast(1)
                            onFastScrollToLine(targetLine)
                        }
                    }
            )
        }

        // Mobile Developer Quick Key Bar (Brackets, Operators, Quotes)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B0F19))
                .border(width = 0.5.dp, color = Color(0xFF1E293B))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val quickTokens = listOf("    ", "(", ")", "{", "}", "[", "]", ":", ";", "=", "\"", "'", "<", ">", "->", "=>", "&", "*", "!", "_")
            items(quickTokens) { token ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF151D2E),
                    modifier = Modifier
                        .clickable {
                            val updated = textContent + token
                            textContent = updated
                            onCodeChange(updated)
                        }
                        .testTag("quick_token_${token.trim().ifEmpty { "tab" }}")
                ) {
                    Text(
                        text = if (token == "    ") "TAB" else token,
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
