package com.example.remindmehere.ui.components

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
import com.example.remindmehere.data.model.ReminderType
import com.example.remindmehere.theme.*
import com.example.remindmehere.ui.viewmodel.CreateReminderViewModel
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
                .navigationBarsPadding()
                .imePadding()
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
                        radiusMeters = state.radiusMeters,
                        pickedLat = state.latitude,
                        pickedLng = state.longitude,
                        onTypeChange = viewModel::updateType,
                        onTimeChange = viewModel::updateTriggerAt,
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
                        Text(if (step == 1) "Next" else "Save Reminder")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
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

@Composable
private fun StepTwo(
    type: ReminderType,
    triggerAt: Long?,
    radiusMeters: Float,
    pickedLat: Double?,
    pickedLng: Double?,
    onTypeChange: (ReminderType) -> Unit,
    onTimeChange: (Long) -> Unit,
    onLocationChange: (Double, Double) -> Unit,
    onRadiusChange: (Float) -> Unit,
    context: android.content.Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
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
                    // Show date/time picker button
                    val cal = remember { Calendar.getInstance() }
                    val label = triggerAt?.let {
                        SimpleDateFormat("EEE, MMM d  •  h:mm a", Locale.getDefault()).format(Date(it))
                    } ?: "Tap to choose date & time"

                    Column {
                        Surface(
                            color = NavyContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d -> cal.set(y, m, d)
                                            TimePickerDialog(context, { _, h, min ->
                                                cal.set(Calendar.HOUR_OF_DAY, h); cal.set(Calendar.MINUTE, min); cal.set(Calendar.SECOND, 0)
                                                onTimeChange(cal.timeInMillis)
                                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
                                        },
                                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                                .border(1.dp, if (triggerAt != null) VioletPrimary.copy(0.5f) else CardBorder, RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.CalendarMonth, null, tint = VioletPrimary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(label, color = if (triggerAt != null) OnSurface else OnSurfaceMuted, style = MaterialTheme.typography.bodyMedium)
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
