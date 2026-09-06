package com.remindmehere.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.AllInbox
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.remindmehere.app.data.model.Reminder
import com.remindmehere.app.data.model.ReminderType
import com.remindmehere.app.theme.*
import com.remindmehere.app.ui.components.CreateReminderSheet
import com.remindmehere.app.ui.components.ReminderCard
import com.remindmehere.app.ui.viewmodel.CreateReminderViewModel
import com.remindmehere.app.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dashboardVm: DashboardViewModel = hiltViewModel(),
    createVm: CreateReminderViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit,
    onNavigateToUpcoming: () -> Unit = {}
) {
    val all by dashboardVm.activeReminders.collectAsStateWithLifecycle()
    val today by dashboardVm.todayReminders.collectAsStateWithLifecycle()
    val scheduled by dashboardVm.scheduledReminders.collectAsStateWithLifecycle()
    val completed by dashboardVm.historyReminders.collectAsStateWithLifecycle()
    val queuedDone by dashboardVm.queuedMarkDoneIds.collectAsStateWithLifecycle()

    var showSheet by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf("All") }

    val displayed = when (filter) {
        "Today"     -> today
        "Scheduled" -> scheduled
        "Completed" -> completed
        else        -> all
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Hero header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(VioletDark.copy(alpha = 0.7f), DeepNavy)))
                    .padding(top = 56.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.Notifications, null, tint = VioletLight, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("RemindMeHere", style = MaterialTheme.typography.labelLarge, color = VioletLight)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(Icons.Outlined.History, contentDescription = "History", tint = VioletLight)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Your Reminders",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp),
                        color = OnSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${all.count { it.status.name == "PENDING" }} pending · ${all.size} total",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted
                    )
                }
            }

            // 2x2 Grid
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardGridCard("Today", Icons.Outlined.Today, today.size, VioletPrimary, filter == "Today", Modifier.weight(1f)) { filter = "Today" }
                    DashboardGridCard("Scheduled", Icons.Outlined.Schedule, scheduled.size, CyanAccent, filter == "Scheduled", Modifier.weight(1f)) { filter = "Scheduled" }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardGridCard("All", Icons.Outlined.AllInbox, all.size, OnSurface, filter == "All", Modifier.weight(1f)) { filter = "All" }
                    DashboardGridCard("Completed", Icons.Outlined.CheckCircle, completed.size, OnSurfaceMuted, filter == "Completed", Modifier.weight(1f)) { filter = "Completed" }
                }
            }

            Text(
                text = "My Lists",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Reminders List Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigateToUpcoming() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Purple circle with list icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(VioletPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.List,
                            contentDescription = "List",
                            tint = OnPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Text(
                        text = "Reminders",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = OnSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = all.size.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceMuted
                    )

                    Spacer(Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "View list",
                        tint = OnSurfaceMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // FAB
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(targetValue = if (isPressed) 0.85f else 1f, label = "fabScale")

        FloatingActionButton(
            onClick = { showSheet = true },
            interactionSource = interactionSource,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .scale(scale),
            containerColor = VioletPrimary,
            contentColor = OnPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Reminder", modifier = Modifier.size(24.dp))
        }

        if (showSheet) {
            CreateReminderSheet(viewModel = createVm, onDismiss = { showSheet = false })
        }
    }
}

@Composable
fun DashboardGridCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    iconColor: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (selected) NavyContainer else NavySurface),
        border = BorderStroke(1.dp, if (selected) VioletPrimary else CardBorder),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
                Spacer(Modifier.weight(1f))
                Text(count.toString(), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
            }
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = OnSurfaceMuted)
        }
    }
}
