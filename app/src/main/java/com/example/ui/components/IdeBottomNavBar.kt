package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.IdeTab

@Composable
fun IdeBottomNavBar(
    currentTab: IdeTab,
    onTabSelected: (IdeTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.testTag("ide_bottom_navigation_bar"),
        containerColor = Color(0xFF090D16),
        tonalElevation = 6.dp
    ) {
        val items = listOf(
            Triple(IdeTab.EDITOR, "Editor", Icons.Default.Code),
            Triple(IdeTab.TERMINAL, "Terminal", Icons.Default.Terminal),
            Triple(IdeTab.ENVIRONMENTS, "Toolchains", Icons.Default.Folder),
            Triple(IdeTab.SEARCH_INDEX, "Index", Icons.Default.Search),
            Triple(IdeTab.OPENCODE_AI, "OpenCode AI", Icons.Default.AutoAwesome)
        )

        items.forEach { (tab, label, icon) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    unselectedIconColor = Color(0xFF64748B),
                    selectedTextColor = Color(0xFF00E5FF),
                    unselectedTextColor = Color(0xFF475569),
                    indicatorColor = Color(0xFF00E5FF)
                ),
                modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
            )
        }
    }
}
