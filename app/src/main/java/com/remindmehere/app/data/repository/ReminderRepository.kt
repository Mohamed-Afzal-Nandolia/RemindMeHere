package com.remindmehere.app.data.repository

import com.remindmehere.app.data.db.ReminderDao
import com.remindmehere.app.data.model.Reminder
import com.remindmehere.app.data.model.ReminderStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val dao: ReminderDao
) {
    val allReminders: Flow<List<Reminder>> = dao.getAllReminders()
    val timeReminders: Flow<List<Reminder>> = dao.getTimeReminders()
    val locationReminders: Flow<List<Reminder>> = dao.getLocationReminders()

    suspend fun addReminder(reminder: Reminder): Long = dao.insert(reminder)
    suspend fun updateReminder(reminder: Reminder) = dao.update(reminder)
    suspend fun deleteReminder(reminder: Reminder) = dao.delete(reminder)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun markDone(id: Long) = dao.updateStatus(id, ReminderStatus.DONE)
    suspend fun markTriggered(id: Long) = dao.updateStatus(id, ReminderStatus.TRIGGERED)
    suspend fun getPendingReminders(): List<Reminder> = dao.getPendingReminders()
    suspend fun getById(id: Long): Reminder? = dao.getById(id)
}
