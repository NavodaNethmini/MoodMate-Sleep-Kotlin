package com.example.moodmatesleep.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_sessions")
data class SleepSession(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Example: "2026-09-10"
    val sleepDate: String,

    // Start Sleep button pressed time
    val bedtimeMillis: Long,

    // I'm Awake button pressed time
    val wakeTimeMillis: Long? = null,

    val durationMinutes: Int? = null,

    // 1 = Very Poor, 5 = Great
    val sleepQuality: Int? = null,

    // Example: "Stress,Phone"
    val disturbances: String? = null,

    val note: String? = null,

    // Happy, Calm, Neutral, Tired, Stressed, Low
    val mood: String? = null,

    // 0 - 100
    val energyLevel: Int? = null,

    // Example: "Sleep,Study"
    val moodFactors: String? = null
)