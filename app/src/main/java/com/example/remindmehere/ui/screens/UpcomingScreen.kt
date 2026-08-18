package com.example.remindmehere.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.remindmehere.data.model.ReminderStatus
import com.example.remindmehere.theme.*
import com.example.remindmehere.ui.components.ReminderCard
import com.example.remindmehere.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun UpcomingScreen(vm: DashboardViewModel = hiltViewModel(), onNavigateToHistory: () -> Unit) {
    val reminders by vm.timeReminders.collectAsStateWithLifecycle()
    val pending = reminders.filter { it.status == ReminderStatus.PENDING }
    val triggered = reminders.filter { it.status == ReminderStatus.TRIGGERED }

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxSize().widthIn(max = 600.dp)) {

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
                        text = "Time-based reminders",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                }
            }

            if (reminders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⏰", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No time reminders", color = OnSurfaceMuted, style = MaterialTheme.typography.bodyLarge)
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
                                ReminderCard(reminder, onMarkDone = { vm.markDone(it) }, onDelete = { vm.deleteReminder(it) })
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
                                ReminderCard(reminder, onMarkDone = { vm.markDone(it) }, onDelete = { vm.deleteReminder(it) })
                            }
                        }
                    }
                }
            }
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
