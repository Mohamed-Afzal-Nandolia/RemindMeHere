package com.example.remindmehere.data.db

import androidx.room.*
import com.example.remindmehere.data.model.Reminder
import com.example.remindmehere.data.model.ReminderStatus
import com.example.remindmehere.data.model.ReminderType
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE type = 'TIME' AND status != 'DONE' ORDER BY triggerAt ASC")
    fun getTimeReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE type = 'LOCATION' AND status != 'DONE'")
    fun getLocationReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE status = 'PENDING'")
    suspend fun getPendingReminders(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): Reminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("UPDATE reminders SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: ReminderStatus)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
