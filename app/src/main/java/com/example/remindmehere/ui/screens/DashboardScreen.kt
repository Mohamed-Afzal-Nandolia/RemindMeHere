package com.example.remindmehere.ui.screens

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
import com.example.remindmehere.data.model.Reminder
import com.example.remindmehere.data.model.ReminderType
import com.example.remindmehere.theme.*
import com.example.remindmehere.ui.components.CreateReminderSheet
import com.example.remindmehere.ui.components.ReminderCard
import com.example.remindmehere.ui.viewmodel.CreateReminderViewModel
import com.example.remindmehere.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dashboardVm: DashboardViewModel = hiltViewModel(),
    createVm: CreateReminderViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit
) {
    val all by dashboardVm.activeReminders.collectAsStateWithLifecycle()
    val time by dashboardVm.timeReminders.collectAsStateWithLifecycle()
    val loc by dashboardVm.locationReminders.collectAsStateWithLifecycle()
    var showSheet by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf("All") }

    val displayed = when (filter) {
        "Time"     -> time
        "Location" -> loc
        else       -> all
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxSize().widthIn(max = 600.dp)) {

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

            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                val filters = listOf("All", "Time", "Location")
                items(filters) { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(f) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VioletPrimary,
                            selectedLabelColor = OnPrimary,
                            containerColor = NavyContainer,
                            labelColor = OnSurfaceMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filter == f,
                            borderColor = CardBorder,
                            selectedBorderColor = VioletPrimary
                        )
                    )
                }
            }

            // Reminder list
            if (displayed.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✨", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No reminders yet", color = OnSurfaceMuted, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(4.dp))
                        Text("Tap + to add one", color = OnSurfaceMuted.copy(0.6f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayed, key = { it.id }) { reminder ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(),
                            modifier = Modifier.animateItem()
                        ) {
                            ReminderCard(
                                reminder = reminder,
                                onMarkDone = { dashboardVm.markDone(it) },
                                onDelete = { dashboardVm.deleteReminder(it) }
                            )
                        }
                    }
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
