package com.example.moodmatesleep.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.moodmatesleep.data.dao.HabitCompletionDao
import com.example.moodmatesleep.data.dao.HabitDao
import com.example.moodmatesleep.data.dao.SleepSessionDao
import com.example.moodmatesleep.data.dao.UserProfileDao
import com.example.moodmatesleep.data.entity.Habit
import com.example.moodmatesleep.data.entity.HabitCompletion
import com.example.moodmatesleep.data.entity.SleepSession
import com.example.moodmatesleep.data.entity.UserProfile

@Database(
    entities = [
        UserProfile::class,
        SleepSession::class,
        Habit::class,
        HabitCompletion::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MoodMateDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao

    abstract fun sleepSessionDao(): SleepSessionDao

    abstract fun habitDao(): HabitDao

    abstract fun habitCompletionDao(): HabitCompletionDao

    companion object {

        @Volatile
        private var INSTANCE: MoodMateDatabase? = null

        fun getDatabase(context: Context): MoodMateDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoodMateDatabase::class.java,
                    "moodmate_sleep_database"
                ).build()

                INSTANCE = instance

                instance
            }
        }
    }
}