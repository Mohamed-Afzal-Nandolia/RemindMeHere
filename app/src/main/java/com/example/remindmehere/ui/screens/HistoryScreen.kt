package com.example.remindmehere.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.remindmehere.theme.*
import com.example.remindmehere.ui.components.ReminderCard
import com.example.remindmehere.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    dashboardVm: DashboardViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val history by dashboardVm.historyReminders.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxSize().widthIn(max = 600.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp, bottom = 16.dp, start = 8.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VioletLight)
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp),
                    color = OnSurface
                )
            }

            // List
            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🕒", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No completed reminders", color = OnSurfaceMuted, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(history, key = { it.id }) { reminder ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(),
                            modifier = Modifier.animateItem()
                        ) {
                            ReminderCard(
                                reminder = reminder,
                                onMarkDone = { }, // Already done
                                onDelete = { dashboardVm.deleteReminder(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
