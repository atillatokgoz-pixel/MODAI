package com.example.naifdeneme.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Pomodoro Seans Varlığı
 * Her tamamlanan pomodoro seansı kaydedilir
 */
@Entity(tableName = "pomodoro_sessions")
data class PomodoroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val startTime: Long = System.currentTimeMillis(),

    val endTime: Long = System.currentTimeMillis(),

    val duration: Int, // dakika cinsinden (25 veya 5)

    val type: PomodoroType, // WORK veya BREAK

    val completed: Boolean = true, // Seans tamamlandı mı?

    val date: Long = System.currentTimeMillis() // Tarih damgası
)

/**
 * Pomodoro Tipi
 */
enum class PomodoroType {
    WORK,  // Çalışma seansı (25dk)
    BREAK  // Mola seansı (5dk)
}