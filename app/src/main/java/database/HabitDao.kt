package com.example.naifdeneme.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getAllHabitsCount(): Int

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: Long): HabitEntity?

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    suspend fun getAllHabitsForWidget(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE reminderEnabled = 1")
    suspend fun getHabitsWithReminder(): List<HabitEntity>

    @Insert
    suspend fun insertHabit(habit: HabitEntity): Long

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Transaction
    suspend fun completeHabit(habitId: Long) {
        val habit = getHabitById(habitId) ?: return
        val today = HabitEntity.getTodayDateString()

        if (habit.lastCompletedDate == today) return

        val newCompletionDates = if (habit.completionDates.isBlank()) {
            today
        } else {
            "${habit.completionDates},$today"
        }

        val yesterday = HabitEntity.getDateStringDaysAgo(1)
        val isYesterdayCompleted = habit.isCompletedOn(yesterday)

        val newStreak = if (isYesterdayCompleted || habit.currentStreak == 0) {
            habit.currentStreak + 1
        } else {
            1
        }

        val newLongestStreak = maxOf(habit.longestStreak, newStreak)

        val totalDays = HabitEntity.getDaysSince(habit.createdAt).coerceAtLeast(1)
        val newTotalCompletions = habit.totalCompletions + 1
        val newStrengthScore = (newTotalCompletions.toFloat() / totalDays).coerceIn(0f, 1f)

        val updatedHabit = habit.copy(
            lastCompletedDate = today,
            currentStreak = newStreak,
            longestStreak = newLongestStreak,
            totalCompletions = newTotalCompletions,
            completionDates = newCompletionDates,
            strengthScore = newStrengthScore
        )

        updateHabit(updatedHabit)
    }

    @Transaction
    suspend fun uncompleteHabit(habitId: Long) {
        val habit = getHabitById(habitId) ?: return
        val today = HabitEntity.getTodayDateString()

        if (habit.lastCompletedDate != today) return

        val dates = habit.completionDates.split(",").filter { it != today }
        val newCompletionDates = dates.joinToString(",")

        val newLastCompletedDate = dates.lastOrNull()

        var newStreak = 0
        if (newLastCompletedDate != null) {
            for (i in 1..365) {
                val date = HabitEntity.getDateStringDaysAgo(i)
                if (dates.contains(date)) {
                    newStreak++
                } else {
                    break
                }
            }
        }

        val totalDays = HabitEntity.getDaysSince(habit.createdAt).coerceAtLeast(1)
        val newTotalCompletions = (habit.totalCompletions - 1).coerceAtLeast(0)
        val newStrengthScore = (newTotalCompletions.toFloat() / totalDays).coerceIn(0f, 1f)

        val updatedHabit = habit.copy(
            lastCompletedDate = newLastCompletedDate,
            currentStreak = newStreak,
            totalCompletions = newTotalCompletions,
            completionDates = newCompletionDates,
            strengthScore = newStrengthScore
        )

        updateHabit(updatedHabit)
    }

    @Transaction
    suspend fun skipHabit(habitId: Long) {
        val habit = getHabitById(habitId) ?: return
        val today = HabitEntity.getTodayDateString()

        val newCompletionDates = if (habit.completionDates.isBlank()) {
            "SKIP_$today"
        } else {
            "${habit.completionDates},SKIP_$today"
        }

        val updatedHabit = habit.copy(
            completionDates = newCompletionDates
        )

        updateHabit(updatedHabit)
    }
}