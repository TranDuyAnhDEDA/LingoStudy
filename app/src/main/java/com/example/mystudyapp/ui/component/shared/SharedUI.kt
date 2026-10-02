package com.example.mystudyapp.ui.component.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mystudyapp.ui.theme.LingoBlue
import com.example.mystudyapp.ui.theme.LingoGray

@Composable
fun LingoStatChip(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.9f),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E3A8A),
                    lineHeight = 16.sp
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 12.sp
                )
            }
        }
    }
}

data class NavItemData(val label: String, val icon: ImageVector)

@Composable
fun LingoBottomNav(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    val navItems = listOf(
        NavItemData("Home",     Icons.Default.Home),
        NavItemData("Learn",    Icons.Default.AutoStories),
        NavItemData("Practice", Icons.Default.SportsEsports),
        NavItemData("Me",       Icons.Default.Person)
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected  = selectedIndex == index,
                onClick   = { onItemSelected(index) },
                icon      = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label     = {
                    Text(
                        text       = item.label,
                        fontSize   = 11.sp,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = LingoBlue,
                    selectedTextColor   = LingoBlue,
                    indicatorColor      = Color(0xFFE0EAFF),
                    unselectedIconColor = LingoGray,
                    unselectedTextColor = LingoGray
                )
            )
        }
    }
}
