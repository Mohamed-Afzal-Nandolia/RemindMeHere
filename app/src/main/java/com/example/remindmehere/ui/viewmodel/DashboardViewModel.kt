package com.example.remindmehere.ui.viewmodel

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remindmehere.alarm.AlarmScheduler
import com.example.remindmehere.data.model.Reminder
import com.example.remindmehere.data.model.ReminderType
import com.example.remindmehere.data.repository.ReminderRepository
import com.example.remindmehere.geofence.GeofenceManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    val allReminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timeReminders: StateFlow<List<Reminder>> = repository.timeReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locationReminders: StateFlow<List<Reminder>> = repository.locationReminders
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
