package com.example.remindmehere.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.remindmehere.theme.*

sealed class BottomNavDest(val route: String, val label: String) {
    object Dashboard : BottomNavDest("dashboard", "All")
    object Upcoming  : BottomNavDest("upcoming", "Upcoming")
    object Nearby    : BottomNavDest("nearby", "Nearby")
}

@Composable
fun BottomNavBar(
    current: BottomNavDest,
    onNavigate: (BottomNavDest) -> Unit
) {
    val items = listOf(BottomNavDest.Dashboard, BottomNavDest.Upcoming, BottomNavDest.Nearby)
    NavigationBar(
        containerColor = NavySurface,
        tonalElevation = 0.dp
    ) {
        items.forEach { dest ->
            val selected = current.route == dest.route
            val iconTint by animateColorAsState(
                if (selected) VioletPrimary else OnSurfaceMuted, label = "tint"
            )
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(dest) },
                icon = {
                    Icon(
                        imageVector = when (dest) {
                            BottomNavDest.Dashboard -> Icons.Outlined.Dashboard
                            BottomNavDest.Upcoming  -> Icons.Outlined.Schedule
                            BottomNavDest.Nearby    -> Icons.Outlined.LocationOn
                        },
                        contentDescription = dest.label,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = dest.label,
                        color = iconTint,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = VioletPrimary.copy(alpha = 0.15f),
                    selectedIconColor = VioletPrimary,
                    selectedTextColor = VioletPrimary
                )
            )
        }
    }
}
