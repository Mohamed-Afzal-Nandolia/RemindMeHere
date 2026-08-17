package com.example.remindmehere.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.remindmehere.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_REMINDER_TITLE = "reminder_title"
        const val EXTRA_REMINDER_NOTE = "reminder_note"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val title = intent.getStringExtra(EXTRA_REMINDER_TITLE) ?: "Reminder"
        val note = intent.getStringExtra(EXTRA_REMINDER_NOTE) ?: ""
        if (id != -1L) {
            notificationHelper.showTimeReminder(id, title, note)
        }
    }
}
