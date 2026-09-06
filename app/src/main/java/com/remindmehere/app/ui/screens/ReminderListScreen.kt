package com.remindmehere.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.remindmehere.app.data.model.Reminder
import com.remindmehere.app.theme.*
import com.remindmehere.app.ui.components.CreateReminderSheet
import com.remindmehere.app.ui.components.ReminderCard
import com.remindmehere.app.ui.viewmodel.CreateReminderViewModel
import com.remindmehere.app.ui.viewmodel.DashboardViewModel

enum class ReminderFilter { ALL, TODAY, SCHEDULED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    filter: ReminderFilter,
    dashboardVm: DashboardViewModel = hiltViewModel(),
    createVm: CreateReminderViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val all by dashboardVm.activeReminders.collectAsStateWithLifecycle()
    val today by dashboardVm.todayReminders.collectAsStateWithLifecycle()
    val scheduled by dashboardVm.scheduledReminders.collectAsStateWithLifecycle()
    val queuedDone by dashboardVm.queuedMarkDoneIds.collectAsStateWithLifecycle()

    var showSheet by remember { mutableStateOf(false) }

    val title = when (filter) {
        ReminderFilter.ALL -> "All"
        ReminderFilter.TODAY -> "Today"
        ReminderFilter.SCHEDULED -> "Scheduled"
    }

    val reminders: List<Reminder> = when (filter) {
        ReminderFilter.ALL -> all
        ReminderFilter.TODAY -> today
        ReminderFilter.SCHEDULED -> scheduled
    }

    val emptyEmoji = when (filter) {
        ReminderFilter.ALL -> "✨"
        ReminderFilter.TODAY -> "☀️"
        ReminderFilter.SCHEDULED -> "📅"
    }

    val emptyText = when (filter) {
        ReminderFilter.ALL -> "No reminders yet"
        ReminderFilter.TODAY -> "Nothing due today"
        ReminderFilter.SCHEDULED -> "No scheduled reminders"
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(VioletDark.copy(alpha = 0.5f), DeepNavy)))
                    .padding(top = 56.dp, bottom = 16.dp, start = 8.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VioletLight)
                    }
                    Spacer(Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            ),
                            color = OnSurface
                        )
                        Text(
                            text = "${reminders.size} reminder${if (reminders.size == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceMuted
                        )
                    }
                }
            }

            // List
            if (reminders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emptyEmoji, fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(emptyText, color = OnSurfaceMuted, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(),
                            modifier = Modifier.animateItem()
                        ) {
                            ReminderCard(
                                reminder = reminder,
                                isQueuedForDone = queuedDone.contains(reminder.id),
                                onClick = {
                                    createVm.loadReminder(reminder)
                                    showSheet = true
                                },
                                onMarkDone = { dashboardVm.queueMarkDone(it) },
                                onUnmarkDone = { dashboardVm.unqueueMarkDone(it) },
                                onDelete = { dashboardVm.deleteReminder(it) }
                            )
                        }
                    }
                }
            }
        }

        if (showSheet) {
            CreateReminderSheet(viewModel = createVm, onDismiss = { showSheet = false })
        }
    }
}
