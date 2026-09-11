package com.example.moodmatesleep.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(

    @PrimaryKey
    val id: Int = 1,

    val name: String,

    // Example: 8 hours = 480 minutes
    val sleepGoalMinutes: Int,

    // Example: "10:30 PM"
    val preferredBedtime: String,

    val bedtimeReminderEnabled: Boolean = true,

    val morningReminderEnabled: Boolean = true,

    // Example: 30 minutes
    val defaultSoundTimerMinutes: Int = 30
)