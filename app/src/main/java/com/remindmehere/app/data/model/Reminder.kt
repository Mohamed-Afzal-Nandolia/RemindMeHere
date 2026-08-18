package com.remindmehere.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReminderType { TIME, LOCATION }
enum class ReminderStatus { PENDING, TRIGGERED, DONE }

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String = "",
    val type: ReminderType,
    val status: ReminderStatus = ReminderStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    // Time-based fields
    val triggerAt: Long? = null,           // epoch millis
    // Location-based fields
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Float = 200f,
    val placeName: String = ""
)
