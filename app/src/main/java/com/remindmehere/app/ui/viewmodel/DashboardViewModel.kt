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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
