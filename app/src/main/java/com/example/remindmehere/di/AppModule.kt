package com.example.remindmehere.di

import android.content.Context
import androidx.room.Room
import com.example.remindmehere.data.db.AppDatabase
import com.example.remindmehere.data.db.ReminderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "remindmehere.db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun provideReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()
}
