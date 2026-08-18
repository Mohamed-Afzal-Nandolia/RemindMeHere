package com.remindmehere.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.remindmehere.app.data.model.ReminderType
import com.remindmehere.app.data.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: ReminderRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            CoroutineScope(Dispatchers.IO).launch {
                // Re-schedule all pending time reminders after reboot
                repository.getPendingReminders()
                    .filter { it.type == ReminderType.TIME && it.triggerAt != null }
                    .filter { it.triggerAt!! > System.currentTimeMillis() }
                    .forEach { alarmScheduler.schedule(it) }
            }
        }
    }
}
