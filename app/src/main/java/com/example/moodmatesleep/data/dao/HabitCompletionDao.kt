package com.example.moodmatesleep.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moodmatesleep.data.entity.HabitCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(
        completion: HabitCompletion
    )

    @Query(
        """
        SELECT * FROM habit_completions
        WHERE completionDate = :date
        """
    )
    fun getCompletionsForDate(
        date: String
    ): Flow<List<HabitCompletion>>

    @Query(
        """
        SELECT * FROM habit_completions
        WHERE habitId = :habitId
        AND completionDate = :date
        LIMIT 1
        """
    )
    suspend fun getCompletion(
        habitId: Long,
        date: String
    ): HabitCompletion?

    @Query(
        """
        SELECT habitId FROM habit_completions
        WHERE completionDate = :date
        AND completed = 1
        """
    )
    suspend fun getCompletedHabitIdsForDate(
        date: String
    ): List<Long>

    @Query(
        """
        DELETE FROM habit_completions
        WHERE habitId = :habitId
        AND completionDate = :date
        """
    )
    suspend fun removeCompletion(
        habitId: Long,
        date: String
    )

    @Query(
        """
        DELETE FROM habit_completions
        WHERE habitId = :habitId
        """
    )
    suspend fun deleteCompletionsForHabit(
        habitId: Long
    )
}