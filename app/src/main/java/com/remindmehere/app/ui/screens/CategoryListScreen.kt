package com.remindmehere.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.remindmehere.app.theme.*
import com.remindmehere.app.ui.components.ReminderCard
import com.remindmehere.app.ui.components.CreateReminderSheet
import com.remindmehere.app.ui.viewmodel.CreateReminderViewModel
import com.remindmehere.app.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    category: String,
    dashboardVm: DashboardViewModel = hiltViewModel(),
    createVm: CreateReminderViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val all by dashboardVm.activeReminders.collectAsStateWithLifecycle()
    val today by dashboardVm.todayReminders.collectAsStateWithLifecycle()
    val scheduled by dashboardVm.scheduledReminders.collectAsStateWithLifecycle()
    val queuedDone by dashboardVm.queuedMarkDoneIds.collectAsStateWithLifecycle()

    var showSheet by remember { mutableStateOf(false) }

    val reminders = when (category) {
        "Today" -> today
        "Scheduled" -> scheduled
        else -> all
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(VioletDark.copy(alpha = 0.5f), DeepNavy)))
                    .padding(top = 56.dp, bottom = 16.dp, start = 8.dp, end = 8.dp)
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
                            text = category,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp),
                            color = OnSurface
                        )
                    }
                }
            }

            // List
            if (reminders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⏰", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No reminders", color = OnSurfaceMuted, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        Box(modifier = Modifier.animateItem()) {
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
