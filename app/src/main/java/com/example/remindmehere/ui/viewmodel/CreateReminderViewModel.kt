package com.example.remindmehere.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remindmehere.alarm.AlarmScheduler
import com.example.remindmehere.data.model.Reminder
import com.example.remindmehere.data.model.ReminderType
import com.example.remindmehere.data.repository.ReminderRepository
import com.example.remindmehere.geofence.GeofenceManager
import com.example.remindmehere.geofence.LocationPollingService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateReminderState(
    val title: String = "",
    val note: String = "",
    val type: ReminderType = ReminderType.TIME,
    // Time
    val triggerAt: Long? = null,
    // Location
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Float = 200f,
    val placeName: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateReminderViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(CreateReminderState())
    val state: StateFlow<CreateReminderState> = _state

    fun updateTitle(v: String)   { _state.value = _state.value.copy(title = v) }
    fun updateNote(v: String)    { _state.value = _state.value.copy(note = v) }
    fun updateType(v: ReminderType) { _state.value = _state.value.copy(type = v) }
    fun updateTriggerAt(v: Long) { _state.value = _state.value.copy(triggerAt = v) }
    fun updateLocation(lat: Double, lng: Double, place: String = "") {
        _state.value = _state.value.copy(latitude = lat, longitude = lng, placeName = place)
    }
    fun updateRadius(v: Float)   { _state.value = _state.value.copy(radiusMeters = v) }

    fun save() {
        val s = _state.value
        if (s.title.isBlank()) { _state.value = s.copy(error = "Title cannot be empty"); return }
        if (s.type == ReminderType.TIME && s.triggerAt == null) { _state.value = s.copy(error = "Please pick a date and time"); return }
        if (s.type == ReminderType.LOCATION && (s.latitude == null || s.longitude == null)) { _state.value = s.copy(error = "Please pin a location on the map"); return }

        _state.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val reminder = Reminder(
                title = s.title.trim(),
                note = s.note.trim(),
                type = s.type,
                triggerAt = s.triggerAt,
                latitude = s.latitude,
                longitude = s.longitude,
                radiusMeters = s.radiusMeters,
                placeName = s.placeName
            )
            val id = repository.addReminder(reminder)
            val saved = reminder.copy(id = id)

            when (s.type) {
                ReminderType.TIME -> alarmScheduler.schedule(saved)
                ReminderType.LOCATION -> {
                    if (isGmsAvailable()) {
                        geofenceManager.register(saved)
                    } else {
                        // Start polling service for non-GMS devices
                        val serviceIntent = Intent(context, LocationPollingService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                    }
                }
            }
            _state.value = _state.value.copy(isLoading = false, isSaved = true)
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
    fun reset() { _state.value = CreateReminderState() }

    private fun isGmsAvailable() =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
}
