package com.remindmehere.app.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindmehere.app.data.model.ReminderType
import com.remindmehere.app.theme.*
import com.remindmehere.app.ui.viewmodel.CreateReminderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderSheet(
    viewModel: CreateReminderViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(1) } // 1 = What, 2 = When/Where

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.reset()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.reset(); onDismiss() },
        containerColor = NavySurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CardBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 1000.dp)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StepDot(active = step >= 1, label = "1")
                        Box(modifier = Modifier.width(24.dp).height(2.dp).background(if (step >= 2) VioletPrimary else CardBorder))
                        StepDot(active = step >= 2, label = "2")
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = if (step == 1) "What to remember?" else "When / Where?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Error
                state.error?.let { err ->
                    Text(
                        text = err,
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = ErrorColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(8.dp))
                }

                AnimatedContent(targetState = step, label = "step") { s ->
                    when (s) {
                        1 -> StepOne(state.title, state.note, viewModel::updateTitle, viewModel::updateNote)
                        2 -> StepTwo(
                            type = state.type,
                            triggerAt = state.triggerAt,
                            hasDate = state.hasDate,
                            hasTime = state.hasTime,
                            radiusMeters = state.radiusMeters,
                            pickedLat = state.latitude,
                            pickedLng = state.longitude,
                            onTypeChange = viewModel::updateType,
                            onTimeChange = viewModel::updateTriggerAt,
                            onHasDateChange = viewModel::updateHasDate,
                            onHasTimeChange = viewModel::updateHasTime,
                            onLocationChange = { lat, lng -> viewModel.updateLocation(lat, lng) },
                            onRadiusChange = viewModel::updateRadius,
                            context = context
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (step == 2) {
                        OutlinedButton(
                            onClick = { step = 1 },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceMuted),
                            border = BorderStroke(1.dp, CardBorder)
                        ) { Text("Back") }
                    }

                    Button(
                        onClick = {
                            if (step == 1) {
                                if (state.title.isBlank()) { viewModel.clearError(); return@Button }
                                viewModel.clearError()
                                step = 2
                            } else {
                                viewModel.save()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = OnPrimary
                            )
                        } else {
                            Text(if (step == 1) "Next" else if (state.id != null) "Update Reminder" else "Save Reminder")
                        }
                    }
                }

                Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun StepDot(active: Boolean, label: String) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) VioletPrimary else NavyContainer)
            .border(1.dp, if (active) VioletPrimary else CardBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (active) OnPrimary else OnSurfaceMuted)
    }
}

@Composable
private fun StepOne(
    title: String, note: String,
    onTitle: (String) -> Unit, onNote: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitle,
            label = { Text("Title *") },
            placeholder = { Text("e.g. Buy 2 litres of milk", color = OnSurfaceMuted) },
            modifier = Modifier.fillMaxWidth(),
            colors = reminderFieldColors(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            leadingIcon = { Icon(Icons.Outlined.EditNote, null, tint = OnSurfaceMuted) }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = note,
            onValueChange = onNote,
            label = { Text("Note (optional)") },
            modifier = Modifier.fillMaxWidth(),
            colors = reminderFieldColors(),
            maxLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            leadingIcon = { Icon(Icons.Outlined.Notes, null, tint = OnSurfaceMuted) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepTwo(
    type: ReminderType,
    triggerAt: Long?,
    hasDate: Boolean,
    hasTime: Boolean,
    radiusMeters: Float,
    pickedLat: Double?,
    pickedLng: Double?,
    onTypeChange: (ReminderType) -> Unit,
    onTimeChange: (Long?) -> Unit,
    onHasDateChange: (Boolean) -> Unit,
    onHasTimeChange: (Boolean) -> Unit,
    onLocationChange: (Double, Double) -> Unit,
    onRadiusChange: (Float) -> Unit,
    context: android.content.Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Type toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NavyContainer),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TypeTab(label = "⏰ Time", selected = type == ReminderType.TIME) { onTypeChange(ReminderType.TIME) }
            TypeTab(label = "📍 Location", selected = type == ReminderType.LOCATION) { onTypeChange(ReminderType.LOCATION) }
        }

        Spacer(Modifier.height(16.dp))

        AnimatedContent(targetState = type, label = "type") { t ->
            when (t) {
                ReminderType.TIME -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavyContainer)
                            .border(1.dp, VioletPrimary.copy(0.3f), RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp)
                    ) {
                        // Date Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = VioletLight)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Date", style = MaterialTheme.typography.bodyLarge, color = OnSurface)
                                val dateText = if (hasDate && triggerAt != null) SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(triggerAt)) else "None"
                                Text(dateText, style = MaterialTheme.typography.bodySmall, color = VioletPrimary)
                            }
                            Switch(checked = hasDate, onCheckedChange = { 
                                onHasDateChange(it)
                                if (it && triggerAt == null) onTimeChange(System.currentTimeMillis())
                            })
                        }
                        
                        AnimatedVisibility(visible = hasDate) {
                            // DatePicker
                            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = triggerAt ?: System.currentTimeMillis())
                            LaunchedEffect(datePickerState.selectedDateMillis) {
                                datePickerState.selectedDateMillis?.let { dateMillis ->
                                    val calendar = Calendar.getInstance().apply { timeInMillis = triggerAt ?: System.currentTimeMillis() }
                                    val newDateCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                                    calendar.set(Calendar.YEAR, newDateCal.get(Calendar.YEAR))
                                    calendar.set(Calendar.DAY_OF_YEAR, newDateCal.get(Calendar.DAY_OF_YEAR))
                                    onTimeChange(calendar.timeInMillis)
                                }
                            }
                            DatePicker(
                                state = datePickerState,
                                modifier = Modifier.fillMaxWidth(),
                                colors = DatePickerDefaults.colors(
                                    containerColor = NavyContainer,
                                    titleContentColor = OnSurface,
                                    headlineContentColor = OnSurface,
                                    weekdayContentColor = OnSurfaceMuted,
                                    subheadContentColor = OnSurfaceMuted,
                                    yearContentColor = OnSurfaceMuted,
                                    currentYearContentColor = VioletPrimary,
                                    selectedYearContentColor = OnPrimary,
                                    selectedYearContainerColor = VioletPrimary,
                                    dayContentColor = OnSurface,
                                    selectedDayContentColor = OnPrimary,
                                    selectedDayContainerColor = VioletPrimary,
                                    todayContentColor = VioletPrimary,
                                    todayDateBorderColor = VioletPrimary
                                ),
                                title = null,
                                headline = null,
                                showModeToggle = false
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = CardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(Modifier.height(8.dp))

                        // Time Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = VioletLight)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Time", style = MaterialTheme.typography.bodyLarge, color = OnSurface)
                                val timeText = if (hasTime && triggerAt != null) SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(triggerAt)) else "None"
                                Text(timeText, style = MaterialTheme.typography.bodySmall, color = VioletPrimary)
                            }
                            Switch(checked = hasTime, onCheckedChange = { 
                                onHasTimeChange(it)
                                if (it && triggerAt == null) onTimeChange(System.currentTimeMillis())
                            })
                        }
                        
                        AnimatedVisibility(visible = hasTime) {
                            // TimePicker
                            val cal = Calendar.getInstance().apply { timeInMillis = triggerAt ?: System.currentTimeMillis() }
                            val timePickerState = rememberTimePickerState(
                                initialHour = cal.get(Calendar.HOUR_OF_DAY),
                                initialMinute = cal.get(Calendar.MINUTE),
                                is24Hour = false
                            )
                            LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                                val calendar = Calendar.getInstance().apply { timeInMillis = triggerAt ?: System.currentTimeMillis() }
                                calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                calendar.set(Calendar.MINUTE, timePickerState.minute)
                                onTimeChange(calendar.timeInMillis)
                            }
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                TimePicker(
                                    state = timePickerState,
                                    colors = TimePickerDefaults.colors(
                                        clockDialColor = NavySurface,
                                        clockDialSelectedContentColor = OnPrimary,
                                        clockDialUnselectedContentColor = OnSurface,
                                        selectorColor = VioletPrimary,
                                        containerColor = NavyContainer,
                                        timeSelectorSelectedContainerColor = VioletPrimary.copy(0.3f),
                                        timeSelectorUnselectedContainerColor = NavySurface,
                                        timeSelectorSelectedContentColor = VioletPrimary,
                                        timeSelectorUnselectedContentColor = OnSurface
                                    )
                                )
                            }
                        }
                    }
                }
                ReminderType.LOCATION -> {
                    Column {
                        Text("Tap on the map to pin a location", style = MaterialTheme.typography.labelMedium, color = OnSurfaceMuted)
                        Spacer(Modifier.height(8.dp))
                        LocationPickerMap(
                            initialLat = pickedLat,
                            initialLng = pickedLng,
                            radiusMeters = radiusMeters,
                            onLocationPicked = onLocationChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, if (pickedLat != null) CyanAccent.copy(0.5f) else CardBorder, RoundedCornerShape(12.dp))
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Alert radius: ${"%.0f".format(radiusMeters)} m",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanAccent
                        )
                        Slider(
                            value = radiusMeters,
                            onValueChange = onRadiusChange,
                            valueRange = 50f..2000f,
                            colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TypeTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) VioletPrimary else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
            color = if (selected) OnPrimary else OnSurfaceMuted
        )
    }
}

@Composable
fun reminderFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = VioletPrimary,
    unfocusedBorderColor = CardBorder,
    focusedLabelColor = VioletPrimary,
    unfocusedLabelColor = OnSurfaceMuted,
    cursorColor = VioletPrimary,
    focusedTextColor = OnSurface,
    unfocusedTextColor = OnSurface,
    unfocusedContainerColor = NavyContainer,
    focusedContainerColor = NavyContainer
)
