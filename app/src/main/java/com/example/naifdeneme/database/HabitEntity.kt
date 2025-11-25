package com.example.naifdeneme.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Locale

enum class HabitType {
    SIMPLE,     // Sadece Tik At
    COUNTABLE,  // Sayı Gir (Sayfa, Bardak)
    TIMED       // Süre Tut (Dakika)
}

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    // 🔥 YENİ EKLENEN ALAN (Şablon sistemi için gerekli)
    val description: String? = null,

    val icon: String = "💪",

    // 🔥 Renk Long formatında
    val color: Long = 0xFFFF6B6B,

    // --- KATEGORİ VE TİP ---
    val category: String = "OTHER",
    val type: HabitType = HabitType.SIMPLE,

    // Hedef Bilgileri
    val targetValue: Int = 1,
    val unit: String = "",

    val createdAt: Long = System.currentTimeMillis(),

    // --- BİLDİRİM AYARLARI ---
    val reminderEnabled: Boolean = false,

    // 🔥 ESKİ SİSTEM — UI BUNU KULLANIYOR
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,

    // 🔥 Opsiyonel yeni format ("09:00")
    val reminderTime: String? = null,

    val reminderDays: String = "1,2,3,4,5,6,7",

    // --- TAKİP DURUMU ---
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: String? = null,
    val totalCompletions: Int = 0,
    val completionDates: String = "",
    val strengthScore: Float = 0f,
    val currentProgress: Int = 0,

    // Diğer alanlar
    val frequency: String = "Daily",
    val priority: Int = 1
) {

    fun isCompletedToday(): Boolean {
        val today = getTodayDateString()
        return lastCompletedDate == today
    }

    fun isCompletedOn(date: String): Boolean {
        return completionDates.split(",").contains(date)
    }

    fun getCompletionRate(): Int {
        if (totalCompletions == 0) return 0
        val daysSinceCreation = getDaysSince(createdAt)
        if (daysSinceCreation == 0) return 0
        return ((totalCompletions.toFloat() / daysSinceCreation) * 100).toInt()
    }

    fun getLast30DaysCompletion(): List<Boolean> {
        val dates = completionDates.split(",").filter { it.isNotBlank() }
        val result = mutableListOf<Boolean>()
        for (i in 29 downTo 0) {
            val date = getDateStringDaysAgo(i)
            result.add(dates.contains(date))
        }
        return result
    }

    fun shouldShowReminderToday(): Boolean {
        if (!reminderEnabled) return false
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val days = reminderDays.split(",").mapNotNull { it.trim().toIntOrNull() }
        return days.contains(today)
    }

    companion object {
        fun getTodayDateString(): String {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(java.util.Date())
        }

        fun getDateStringDaysAgo(daysAgo: Int): String {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(calendar.time)
        }

        fun getDaysSince(timestamp: Long): Int {
            val diff = System.currentTimeMillis() - timestamp
            val days = (diff / (1000 * 60 * 60 * 24)).toInt()
            return if (days < 1) 1 else days
        }
    }
}
