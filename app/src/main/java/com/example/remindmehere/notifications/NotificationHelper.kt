package com.example.remindmehere.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.remindmehere.MainActivity
import com.example.remindmehere.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_TIME = "remindmehere_time"
        const val CHANNEL_LOCATION = "remindmehere_location"
        const val CHANNEL_POLLING = "remindmehere_polling"
        const val POLLING_NOTIFICATION_ID = 9999
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_TIME, "Time Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for time-based reminders"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_LOCATION, "Location Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications when you arrive at a saved location"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_POLLING, "Location Monitoring", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Background location monitoring service"
            }
        )
    }

    private fun tapIntent(): PendingIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        },
        PendingIntent.FLAG_IMMUTABLE
    )

    fun showTimeReminder(id: Long, title: String, note: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_TIME)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ $title")
            .setContentText(note.ifBlank { "Time to act on your reminder!" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(note.ifBlank { "Time to act on your reminder!" }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapIntent())
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id.toInt(), notification)
        } catch (_: SecurityException) { /* Permission not granted */ }
    }

    fun showLocationReminder(id: Long, title: String, placeName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_LOCATION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📍 $title")
            .setContentText("You're near ${placeName.ifBlank { "your saved location" }}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapIntent())
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id.toInt(), notification)
        } catch (_: SecurityException) { /* Permission not granted */ }
    }

    fun buildPollingForegroundNotification() =
        NotificationCompat.Builder(context, CHANNEL_POLLING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("RemindMeHere")
            .setContentText("Monitoring your location for nearby reminders")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
}
