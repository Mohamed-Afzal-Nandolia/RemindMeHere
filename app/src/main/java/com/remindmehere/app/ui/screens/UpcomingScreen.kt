package com.remindmehere.app.ui.screens

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
import androidx.compose.material.icons.outlined.Schedule
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
import com.remindmehere.app.data.model.ReminderStatus
import com.remindmehere.app.theme.*
import com.remindmehere.app.ui.components.ReminderCard
import com.remindmehere.app.ui.components.CreateReminderSheet
import com.remindmehere.app.ui.viewmodel.CreateReminderViewModel
import com.remindmehere.app.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun UpcomingScreen(
    vm: DashboardViewModel = hiltViewModel(),
    createVm: CreateReminderViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit
) {
    val allReminders by vm.activeReminders.collectAsStateWithLifecycle()
    val timeReminders by vm.timeReminders.collectAsStateWithLifecycle()
    val locReminders by vm.locationReminders.collectAsStateWithLifecycle()
    val queuedDone by vm.queuedMarkDoneIds.collectAsStateWithLifecycle()

    var filter by remember { mutableStateOf("All") }
    var showSheet by remember { mutableStateOf(false) }

    val displayed = when (filter) {
        "Time" -> timeReminders
        "Location" -> locReminders
        else -> allReminders
    }

    val pending = displayed.filter { it.status == ReminderStatus.PENDING }
    val triggered = displayed.filter { it.status == ReminderStatus.TRIGGERED }

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(VioletDark.copy(0.5f), DeepNavy)))
                    .padding(top = 56.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.Schedule, null, tint = VioletLight, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Upcoming", style = MaterialTheme.typography.labelLarge, color = VioletLight)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(Icons.Outlined.History, contentDescription = "History", tint = VioletLight)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "All your pending reminders",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
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

            if (displayed.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⏰", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No upcoming reminders", color = OnSurfaceMuted, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (pending.isNotEmpty()) {
                        item {
                            SectionLabel("Upcoming (${pending.size})")
                        }
                        items(pending, key = { it.id }) { reminder ->
                            Column(modifier = Modifier.animateItem()) {
                                ReminderCard(
                                    reminder = reminder,
                                    isQueuedForDone = queuedDone.contains(reminder.id),
                                    onClick = {
                                        createVm.loadReminder(reminder)
                                        showSheet = true
                                    },
                                    onMarkDone = { vm.queueMarkDone(it) },
                                    onUnmarkDone = { vm.unqueueMarkDone(it) },
                                    onDelete = { vm.deleteReminder(it) }
                                )
                                reminder.triggerAt?.let { at ->
                                    val diff = at - System.currentTimeMillis()
                                    val label = when {
                                        diff < 0 -> "Overdue"
                                        diff < TimeUnit.HOURS.toMillis(1) -> "In ${TimeUnit.MILLISECONDS.toMinutes(diff)} min"
                                        diff < TimeUnit.DAYS.toMillis(1) -> "In ${TimeUnit.MILLISECONDS.toHours(diff)} hours"
                                        else -> SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(at))
                                    }
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (diff < 0) ErrorColor else CyanAccent,
                                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (triggered.isNotEmpty()) {
                        item { Spacer(Modifier.height(8.dp)); SectionLabel("Triggered (${triggered.size})") }
                        items(triggered, key = { it.id }) { reminder ->
                            Box(modifier = Modifier.animateItem()) {
                                ReminderCard(
                                    reminder = reminder,
                                    isQueuedForDone = queuedDone.contains(reminder.id),
                                    onClick = {
                                        createVm.loadReminder(reminder)
                                        showSheet = true
                                    },
                                    onMarkDone = { vm.queueMarkDone(it) },
                                    onUnmarkDone = { vm.unqueueMarkDone(it) },
                                    onDelete = { vm.deleteReminder(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
        if (showSheet) {
            CreateReminderSheet(viewModel = createVm, onDismiss = { showSheet = false })
        }

        // FAB
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(targetValue = if (isPressed) 0.85f else 1f, label = "fabScale")

        FloatingActionButton(
            onClick = { createVm.reset(); showSheet = true },
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
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = OnSurfaceMuted,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
