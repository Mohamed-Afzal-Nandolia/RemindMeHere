package com.remindmehere.app.data.db

import androidx.room.TypeConverter
import com.remindmehere.app.data.model.ReminderStatus
import com.remindmehere.app.data.model.ReminderType

class Converters {
    @TypeConverter fun fromType(v: ReminderType) = v.name
    @TypeConverter fun toType(v: String) = ReminderType.valueOf(v)
    @TypeConverter fun fromStatus(v: ReminderStatus) = v.name
    @TypeConverter fun toStatus(v: String) = ReminderStatus.valueOf(v)
}
