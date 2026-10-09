package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SourceFile

@Composable
fun IdeTabBar(
    openFiles: List<SourceFile>,
    activeFileIndex: Int,
    onSelectFile: (Int) -> Unit,
    onCloseFile: (Int) -> Unit,
    onAddNewFile: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF090D16)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(openFiles) { index, file ->
                val isActive = index == activeFileIndex

                Surface(
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    color = if (isActive) Color(0xFF0F172A) else Color(0xFF060910),
                    modifier = Modifier
                        .clickable { onSelectFile(index) }
                        .testTag("file_tab_${file.name}")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = file.name,
                            color = if (isActive) Color(0xFF00E5FF) else Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )

                        if (file.isModified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "●", color = Color(0xFFFF9900), fontSize = 10.sp)
                        }

                        if (openFiles.size > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onCloseFile(index) },
                                modifier = Modifier
                                    .size(16.dp)
                                    .testTag("close_file_tab_${file.name}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close ${file.name}",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                IconButton(
                    onClick = onAddNewFile,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("tab_bar_add_file_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add New Source File",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
