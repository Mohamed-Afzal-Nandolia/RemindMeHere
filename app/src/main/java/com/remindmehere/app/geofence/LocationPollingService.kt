package com.remindmehere.app.geofence

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import com.remindmehere.app.data.model.ReminderStatus
import com.remindmehere.app.data.model.ReminderType
import com.remindmehere.app.data.repository.ReminderRepository
import com.remindmehere.app.notifications.NotificationHelper
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class LocationPollingService : Service() {

    @Inject lateinit var repository: ReminderRepository
    @Inject lateinit var notificationHelper: NotificationHelper

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            lastLocation = result.lastLocation
            lastLocation?.let { checkGeofences(it) }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NotificationHelper.POLLING_NOTIFICATION_ID,
            notificationHelper.buildPollingForegroundNotification()
        )
        requestLocationUpdates()
        return START_STICKY
    }

    @Suppress("MissingPermission")
    private fun requestLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 60_000L)
            .setMinUpdateDistanceMeters(30f)
            .build()
        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private fun checkGeofences(location: Location) {
        scope.launch {
            val pending = repository.getPendingReminders()
                .filter { it.type == ReminderType.LOCATION }
                .filter { it.status == ReminderStatus.PENDING }

            pending.forEach { reminder ->
                val lat = reminder.latitude ?: return@forEach
                val lng = reminder.longitude ?: return@forEach
                val results = FloatArray(1)
                Location.distanceBetween(location.latitude, location.longitude, lat, lng, results)
                if (results[0] <= reminder.radiusMeters) {
                    notificationHelper.showLocationReminder(reminder.id, reminder.title, reminder.placeName)
                    repository.markTriggered(reminder.id)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(locationCallback)
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
