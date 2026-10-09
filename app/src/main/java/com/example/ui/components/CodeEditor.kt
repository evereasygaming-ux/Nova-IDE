package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AutoIndentationEngine
import com.example.engine.SyntaxColorScheme
import com.example.engine.SyntaxHighlighter
import com.example.model.SupportedLanguage

/**
 * Extended CodeEditor component in Kotlin Jetpack Compose supporting:
 * - Dynamic color schemes (Light and Dark themes)
 * - Intelligent automatic indentation detecting language guidelines (Java, Kotlin, Python, C++, Rust)
 * - Auto-formatting according to standard style guidelines.
 */
@Composable
fun CodeEditor(
    code: String,
    language: SupportedLanguage,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    colorScheme: SyntaxColorScheme = SyntaxColorScheme.CyberDark,
    onColorSchemeChange: ((SyntaxColorScheme) -> Unit)? = null,
    enableAutoIndent: Boolean = true,
    showThemeToggle: Boolean = true,
    readOnly: Boolean = false,
    fontSizeSp: Int = 13
) {
    // Current active color scheme (internal state if not controlled externally)
    var activeScheme by remember(colorScheme) { mutableStateOf(colorScheme) }

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    val lines = remember(code) { code.lines() }
    val lineCount = lines.size.coerceAtLeast(1)

    // Smooth animated color transitions when toggling schemes
    val animatedBg by animateColorAsState(activeScheme.background, tween(300), label = "bg")
    val animatedGutterBg by animateColorAsState(activeScheme.gutterBackground, tween(300), label = "gutterBg")
    val animatedGutterText by animateColorAsState(activeScheme.gutterText, tween(300), label = "gutterText")
    val animatedDivider by animateColorAsState(activeScheme.gutterDivider, tween(300), label = "divider")
    val animatedTextColor by animateColorAsState(activeScheme.textColor, tween(300), label = "textColor")
    val animatedCursorColor by animateColorAsState(activeScheme.cursorColor, tween(300), label = "cursor")

    // Syntax-highlighted code matching current scheme
    val highlightedCode = remember(code, language, activeScheme) {
        SyntaxHighlighter.highlight(code, language, activeScheme)
    }

    val lineHeightSp = (fontSizeSp * 1.5).sp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(animatedBg)
            .testTag("code_editor_container")
    ) {
        // Optional Editor Header Bar with Dynamic Theme Switcher & Auto-Format Action
        if (showThemeToggle) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = animatedGutterBg,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (activeScheme.isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                        ) {
                            Text(
                                text = "${language.displayName} (4 spaces)",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = animatedTextColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${lines.size} lines",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = animatedGutterText
                        )
                    }

                    // Format & Theme Toggle Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Quick Format Button according to language style guide
                        TextButton(
                            onClick = {
                                val formatted = AutoIndentationEngine.formatCode(code, language)
                                onCodeChange(formatted)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("code_editor_format_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "Format Code",
                                tint = if (activeScheme.isDark) Color(0xFF00E5FF) else Color(0xFF0284C7),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Format",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (activeScheme.isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)
                            )
                        }

                        FilledTonalIconButton(
                            onClick = {
                                val newScheme = if (activeScheme.isDark) {
                                    SyntaxColorScheme.SolarLight
                                } else {
                                    SyntaxColorScheme.CyberDark
                                }
                                activeScheme = newScheme
                                onColorSchemeChange?.invoke(newScheme)
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("code_editor_theme_toggle_button"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (activeScheme.isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                contentColor = animatedTextColor
                            )
                        ) {
                            Icon(
                                imageVector = if (activeScheme.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (activeScheme.isDark) "Switch to Light Theme" else "Switch to Dark Theme",
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // Code Editor Body with Gutter and Text Area
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(verticalScroll)
        ) {
            // Line Numbers Gutter
            Column(
                modifier = Modifier
                    .background(animatedGutterBg)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("code_editor_gutter"),
                horizontalAlignment = Alignment.End
            ) {
                for (lineNum in 1..lineCount) {
                    Text(
                        text = "$lineNum",
                        color = animatedGutterText,
                        fontSize = fontSizeSp.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = lineHeightSp
                    )
                }
            }

            // Gutter Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                .background(animatedDivider)
            )

            // Text Input & Syntax Highlighting Overlay Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScroll)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("code_editor_text_area")
            ) {
                // Render syntax-colored annotated text
                Text(
                    text = highlightedCode,
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSizeSp.sp,
                    lineHeight = lineHeightSp,
                    color = Color.Transparent
                )

                // Interactive text input area with automatic indentation
                BasicTextField(
                    value = code,
                    onValueChange = { newTypedText ->
                        val processedText = if (enableAutoIndent) {
                            AutoIndentationEngine.processTextChange(
                                oldText = code,
                                newText = newTypedText,
                                language = language
                            )
                        } else {
                            newTypedText
                        }
                        onCodeChange(processedText)
                    },
                    readOnly = readOnly,
                    textStyle = TextStyle(
                        color = animatedTextColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = fontSizeSp.sp,
                        lineHeight = lineHeightSp
                    ),
                    cursorBrush = SolidColor(animatedCursorColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("code_editor_input_field")
                )
            }
        }
    }
}
