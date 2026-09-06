package com.remindmehere.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindmehere.app.data.model.Reminder
import com.remindmehere.app.data.model.ReminderStatus
import com.remindmehere.app.data.model.ReminderType
import com.remindmehere.app.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCard(
    reminder: Reminder,
    isQueuedForDone: Boolean = false,
    onClick: () -> Unit = {},
    onMarkDone: (Reminder) -> Unit,
    onUnmarkDone: (Reminder) -> Unit = {},
    onDelete: (Reminder) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDeleted by remember { mutableStateOf(false) }

    LaunchedEffect(isDeleted) {
        if (isDeleted) {
            delay(400)
            onDelete(reminder)
        }
    }

    AnimatedVisibility(
        visible = !isDeleted,
        exit = shrinkVertically() + fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
    ) {
        val isDone = reminder.status == ReminderStatus.DONE || isQueuedForDone
        val isTriggered = reminder.status == ReminderStatus.TRIGGERED

        val cardAlpha by animateFloatAsState(if (isDone) 0.5f else 1f, label = "alpha")
        val accentColor = if (reminder.type == ReminderType.TIME) VioletPrimary else CyanAccent

        val density = LocalDensity.current
        val revealOffset = with(density) { -80.dp.toPx() }

        var isRevealed by remember { mutableStateOf(false) }
        var isDragging by remember { mutableStateOf(false) }
        var dragOffset by remember { mutableFloatStateOf(0f) }

        val animatedOffset by animateFloatAsState(
            targetValue = if (isRevealed) revealOffset else 0f,
            label = "offset"
        )
        val currentOffset = if (isDragging) dragOffset else animatedOffset

        Box(
            modifier = modifier
                .padding(vertical = 4.dp)
                .clipToBounds()
        ) {
            // Background (Delete Button)
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp)
                        .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                        .background(ErrorColor.copy(alpha = 0.8f))
                        .clickable { isDeleted = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 20.dp).size(28.dp)
                    )
                }
            }

            // Foreground (Card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(currentOffset.roundToInt(), 0) }
                    .background(DeepNavy, RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                isDragging = true
                                dragOffset = if (isRevealed) revealOffset else 0f
                            },
                            onDragEnd = {
                                isDragging = false
                                isRevealed = dragOffset < revealOffset / 2
                            },
                            onDragCancel = {
                                isDragging = false
                                isRevealed = false
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset = (dragOffset + dragAmount).coerceIn(revealOffset, 0f)
                            }
                        )
                    }
                    .pointerInput(isRevealed) {
                        if (isRevealed) {
                            detectTapGestures(
                                onTap = { isRevealed = false }
                            )
                        } else {
                            detectTapGestures(
                                onTap = { onClick() }
                            )
                        }
                    }
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Radio button for checkmark
                IconButton(
                    onClick = {
                        when {
                            isQueuedForDone -> onUnmarkDone(reminder)   // cancel the pending mark
                            !isDone -> onMarkDone(reminder)             // start the 2-sec queue
                            // if truly done (in DB), do nothing
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = "Mark done",
                        tint = if (isDone) VioletPrimary else OnSurfaceMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Title
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
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
            }
        }
    }
}
}
