package com.example.moodmatesleep.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_completions")
data class HabitCompletion(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val habitId: Long,

    // Example: "2026-09-10"
    val completionDate: String,

    val completed: Boolean = true
)