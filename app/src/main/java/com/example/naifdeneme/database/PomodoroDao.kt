package com.example.naifdeneme.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Pomodoro Data Access Object
 */
@Dao
interface PomodoroDao {

    @Insert
    suspend fun insertSession(session: PomodoroEntity)

    @Query("SELECT * FROM pomodoro_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<PomodoroEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE date >= :startOfDay AND date <= :endOfDay ORDER BY date DESC")
    fun getTodaySessions(startOfDay: Long, endOfDay: Long): Flow<List<PomodoroEntity>>

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE type = 'WORK' AND completed = 1 AND date >= :startOfDay AND date <= :endOfDay")
    fun getTodayWorkSessionCount(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("DELETE FROM pomodoro_sessions WHERE id = :id")
    suspend fun deleteSession(id: Int)

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun deleteAllSessions()
}