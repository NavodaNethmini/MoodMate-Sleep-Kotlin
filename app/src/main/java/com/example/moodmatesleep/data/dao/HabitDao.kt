package com.example.moodmatesleep.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.moodmatesleep.data.entity.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Insert
    suspend fun insertHabits(habits: List<Habit>)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query(
        """
        SELECT * FROM habits
        WHERE isActive = 1
        ORDER BY id ASC
        """
    )
    fun getAllActiveHabits(): Flow<List<Habit>>

    @Query(
        """
        SELECT * FROM habits
        WHERE category = :category
        AND isActive = 1
        ORDER BY id ASC
        """
    )
    fun getHabitsByCategory(category: String): Flow<List<Habit>>

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitCount(): Int
}