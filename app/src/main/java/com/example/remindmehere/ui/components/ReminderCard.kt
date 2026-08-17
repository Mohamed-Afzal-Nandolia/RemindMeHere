package com.example.remindmehere.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remindmehere.data.model.Reminder
import com.example.remindmehere.data.model.ReminderStatus
import com.example.remindmehere.data.model.ReminderType
import com.example.remindmehere.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReminderCard(
    reminder: Reminder,
    onMarkDone: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = reminder.status == ReminderStatus.DONE
    val isTriggered = reminder.status == ReminderStatus.TRIGGERED

    val cardAlpha by animateFloatAsState(if (isDone) 0.5f else 1f, label = "alpha")
    val accentColor = if (reminder.type == ReminderType.TIME) VioletPrimary else CyanAccent

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.4f * cardAlpha), CardBorder)),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyContainer.copy(alpha = cardAlpha)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Type icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (reminder.type == ReminderType.TIME) Icons.Outlined.AccessTime else Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (isDone) OnSurfaceMuted else OnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (reminder.note.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = reminder.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Trigger info chip
                val chipText = when (reminder.type) {
                    ReminderType.TIME -> reminder.triggerAt?.let {
                        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(it))
                    } ?: "No time set"
                    ReminderType.LOCATION -> reminder.placeName.ifBlank { "%.4f, %.4f".format(reminder.latitude, reminder.longitude) }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = accentColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = chipText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (isTriggered && !isDone) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = WarningColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Triggered",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningColor
                            )
                        }
                    }

                    if (isDone) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = SuccessColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Done",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = SuccessColor
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!isDone) {
                    IconButton(
                        onClick = { onMarkDone(reminder) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Mark done",
                            tint = SuccessColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                IconButton(
                    onClick = { onDelete(reminder) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = ErrorColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
