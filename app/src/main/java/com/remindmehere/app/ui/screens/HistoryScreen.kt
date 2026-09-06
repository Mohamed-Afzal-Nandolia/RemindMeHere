package com.remindmehere.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.remindmehere.app.data.model.Reminder
import com.remindmehere.app.theme.*
import com.remindmehere.app.ui.components.ReminderCard
import com.remindmehere.app.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    dashboardVm: DashboardViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val history by dashboardVm.historyReminders.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showClearConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(history.isEmpty()) {
        if (history.isEmpty()) isSelectionMode = false
    }

    val hasSelection = selectedIds.isNotEmpty()

    // ── Clear All confirmation dialog ────────────────────────────────────
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = NavySurface,
            title = { Text("Clear History", color = OnSurface, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Delete all ${history.size} completed reminder${if (history.size == 1) "" else "s"}? This cannot be undone.",
                    color = OnSurfaceMuted
                )
            },
            confirmButton = {
                TextButton(onClick = { dashboardVm.clearHistory(history); showClearConfirm = false }) {
                    Text("Clear All", color = ErrorColor, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = OnSurfaceMuted)
                }
            }
        )
    }

    // ── Main layout ──────────────────────────────────────────────────────
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
                    IconButton(onClick = {
                        if (isSelectionMode) { isSelectionMode = false; selectedIds = emptySet() }
                        else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VioletLight)
                    }
                    Spacer(Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp),
                            color = OnSurface
                        )
                        if (isSelectionMode) {
                            Text(
                                text = if (hasSelection) "${selectedIds.size} selected" else "Tap to select",
                                style = MaterialTheme.typography.labelMedium,
                                color = VioletLight
                            )
                        }
                    }
                    if (!isSelectionMode && history.isNotEmpty()) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Outlined.MoreVert, "Menu", tint = VioletLight)
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                containerColor = NavyContainer
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Select Reminders", color = OnSurface) },
                                    onClick = {
                                        showMenu = false
                                        isSelectionMode = true
                                        selectedIds = emptySet()
                                    }
                                )
                                HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                                DropdownMenuItem(
                                    text = { Text("Clear All", color = ErrorColor) },
                                    onClick = {
                                        showMenu = false
                                        showClearConfirm = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // List
            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No completed reminders", color = OnSurfaceMuted, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp,
                        bottom = if (isSelectionMode) 88.dp else 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(history, key = { it.id }) { reminder ->
                        SelectableReminderRow(
                            reminder = reminder,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedIds.contains(reminder.id),
                            onToggleSelect = {
                                selectedIds = if (selectedIds.contains(reminder.id))
                                    selectedIds - reminder.id
                                else
                                    selectedIds + reminder.id
                            },
                            onDelete = { dashboardVm.deleteReminder(it) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        // ── Bottom action bar (selection mode only) ───────────────────────
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyContainer)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        dashboardVm.markIncomplete(history.filter { selectedIds.contains(it.id) })
                        selectedIds = emptySet()
                        isSelectionMode = false
                    },
                    enabled = hasSelection,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VioletPrimary,
                        disabledContainerColor = VioletPrimary.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Undo, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Mark Incomplete")
                }

                IconButton(
                    onClick = {
                        val toDelete = history.filter { selectedIds.contains(it.id) }
                        dashboardVm.deleteMultiple(toDelete)
                        selectedIds = emptySet()
                        if (history.size == toDelete.size) isSelectionMode = false
                    },
                    enabled = hasSelection,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (hasSelection) ErrorColor.copy(alpha = 0.15f) else ErrorColor.copy(alpha = 0.05f))
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete selected",
                        tint = if (hasSelection) ErrorColor else ErrorColor.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectableReminderRow(
    reminder: Reminder,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onDelete: (Reminder) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AnimatedVisibility(visible = isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, VioletPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isSelectionMode) Modifier.clickable { onToggleSelect() } else Modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = VioletPrimary,
                        uncheckedColor = OnSurfaceMuted,
                        checkmarkColor = OnPrimary
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                ReminderCard(
                    reminder = reminder,
                    onMarkDone = { },
                    onDelete = { if (!isSelectionMode) onDelete(it) }
                )
            }
        }
    }
}
