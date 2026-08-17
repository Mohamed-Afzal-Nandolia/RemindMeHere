package com.example.remindmehere.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.remindmehere.data.repository.ReminderRepository
import com.example.remindmehere.notifications.NotificationHelper
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.Geofence
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var repository: ReminderRepository

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return

        event.triggeringGeofences?.forEach { geofence ->
            val reminderId = geofence.requestId.toLongOrNull() ?: return@forEach
            CoroutineScope(Dispatchers.IO).launch {
                val reminder = repository.getById(reminderId) ?: return@launch
                notificationHelper.showLocationReminder(reminderId, reminder.title, reminder.placeName)
                repository.markTriggered(reminderId)
            }
        }
    }
}
