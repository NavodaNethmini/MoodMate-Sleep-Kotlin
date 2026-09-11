package com.example.moodmatesleep.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.moodmatesleep.data.entity.SleepSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepSessionDao {

    @Insert
    suspend fun insertSession(session: SleepSession): Long

    @Update
    suspend fun updateSession(session: SleepSession)

    @Delete
    suspend fun deleteSession(session: SleepSession)

    @Query(
        """
        SELECT * FROM sleep_sessions
        ORDER BY bedtimeMillis DESC
        """
    )
    fun getAllSessions(): Flow<List<SleepSession>>

    @Query(
        """
        SELECT * FROM sleep_sessions
        ORDER BY bedtimeMillis DESC
        LIMIT 1
        """
    )
    suspend fun getLatestSession(): SleepSession?

    @Query(
        """
        SELECT * FROM sleep_sessions
        WHERE id = :sessionId
        LIMIT 1
        """
    )
    suspend fun getSessionById(sessionId: Long): SleepSession?

    @Query(
        """
        SELECT * FROM sleep_sessions
        WHERE sleepDate = :date
        ORDER BY bedtimeMillis DESC
        """
    )
    suspend fun getSessionsForDate(date: String): List<SleepSession>

    @Query("DELETE FROM sleep_sessions")
    suspend fun deleteAllSessions()
}