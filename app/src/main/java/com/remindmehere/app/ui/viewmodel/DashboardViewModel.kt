package com.remindmehere.app.ui.viewmodel

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remindmehere.app.alarm.AlarmScheduler
import com.remindmehere.app.data.model.Reminder
import com.remindmehere.app.data.model.ReminderType
import com.remindmehere.app.data.repository.ReminderRepository
import com.remindmehere.app.geofence.GeofenceManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val activeReminders: StateFlow<List<Reminder>> = repository.allReminders
        .map { list -> list.filter { it.status != com.remindmehere.app.data.model.ReminderStatus.DONE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyReminders: StateFlow<List<Reminder>> = repository.allReminders
        .map { list -> list.filter { it.status == com.remindmehere.app.data.model.ReminderStatus.DONE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timeReminders: StateFlow<List<Reminder>> = repository.timeReminders
        .map { list -> list.filter { it.status != com.remindmehere.app.data.model.ReminderStatus.DONE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locationReminders: StateFlow<List<Reminder>> = repository.locationReminders
        .map { list -> list.filter { it.status != com.remindmehere.app.data.model.ReminderStatus.DONE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledReminders: StateFlow<List<Reminder>> = timeReminders

    val todayReminders: StateFlow<List<Reminder>> = repository.timeReminders
        .map { list ->
            val todayEnd = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.timeInMillis
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis
            list.filter { it.status != com.remindmehere.app.data.model.ReminderStatus.DONE && it.triggerAt != null && it.triggerAt in todayStart..todayEnd }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _queuedMarkDone = MutableStateFlow<Map<Long, Reminder>>(emptyMap())
    val queuedMarkDoneIds: StateFlow<Set<Long>> = _queuedMarkDone.map { it.keys }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private var markDoneJob: kotlinx.coroutines.Job? = null

    fun queueMarkDone(reminder: Reminder) {
        _queuedMarkDone.value = _queuedMarkDone.value + (reminder.id to reminder)
        
        markDoneJob?.cancel()
        markDoneJob = viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            val currentQueued = _queuedMarkDone.value
            _queuedMarkDone.value = emptyMap()
            currentQueued.values.forEach { r ->
                markDone(r)
            }
        }
    }

    fun unqueueMarkDone(reminder: Reminder) {
        val updated = _queuedMarkDone.value - reminder.id
        _queuedMarkDone.value = updated
        // if nothing left in the queue, cancel the pending commit entirely
        if (updated.isEmpty()) {
            markDoneJob?.cancel()
            markDoneJob = null
        }
        // if there are still other items queued, let the existing job finish for them
    }

    fun markDone(reminder: Reminder) {
        viewModelScope.launch {
            repository.markDone(reminder.id)
            when (reminder.type) {
                ReminderType.TIME -> alarmScheduler.cancel(reminder.id)
                ReminderType.LOCATION -> {
                    if (isGmsAvailable()) geofenceManager.remove(reminder.id)
                }
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            when (reminder.type) {
                ReminderType.TIME -> alarmScheduler.cancel(reminder.id)
                ReminderType.LOCATION -> {
                    if (isGmsAvailable()) geofenceManager.remove(reminder.id)
                }
            }
        }
    }

    private fun isGmsAvailable() =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
}
