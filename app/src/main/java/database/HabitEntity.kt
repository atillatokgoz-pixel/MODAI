package com.example.naifdeneme.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Temel bilgiler
    val name: String,
    val icon: String = "💪",
    val color: String = "#FF6B6B",
    val createdAt: Long = System.currentTimeMillis(),

    // Bildirim ayarları
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val reminderDays: String = "1,2,3,4,5,6,7", // 1=Pzt, 7=Paz

    // Streak bilgileri
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: String? = null, // "2025-10-26"

    // İstatistikler
    val totalCompletions: Int = 0,
    val completionDates: String = "", // "2025-10-23,2025-10-24,2025-10-25"

    // Strength Score (Loop tarzı)
    val strengthScore: Float = 0f // 0.0 - 1.0
) {
    /**
     * Bugün tamamlandı mı?
     */
    fun isCompletedToday(): Boolean {
        val today = getTodayDateString()
        return lastCompletedDate == today
    }

    /**
     * Belirli bir günde tamamlandı mı?
     */
    fun isCompletedOn(date: String): Boolean {
        return completionDates.split(",").contains(date)
    }

    /**
     * Son 30 günün tamamlanma durumu
     */
    fun getLast30DaysCompletion(): List<Boolean> {
        val dates = completionDates.split(",").filter { it.isNotBlank() }
        val result = mutableListOf<Boolean>()

        for (i in 29 downTo 0) {
            val date = getDateStringDaysAgo(i)
            result.add(dates.contains(date))
        }

        return result
    }

    /**
     * Tamamlanma oranı (%)
     */
    fun getCompletionRate(): Int {
        if (totalCompletions == 0) return 0

        val daysSinceCreation = getDaysSince(createdAt)
        if (daysSinceCreation == 0) return 0

        return ((totalCompletions.toFloat() / daysSinceCreation) * 100).toInt()
    }

    /**
     * Bildirim gönderilmeli mi? (bugün için)
     */
    fun shouldShowReminderToday(): Boolean {
        if (!reminderEnabled) return false

        val today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
        val days = reminderDays.split(",").map { it.toIntOrNull() }

        return days.contains(today)
    }

    companion object {
        /**
         * Bugünün tarihini string olarak döndürür
         */
        fun getTodayDateString(): String {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            return sdf.format(java.util.Date())
        }

        /**
         * X gün öncesinin tarihini döndürür
         */
        fun getDateStringDaysAgo(daysAgo: Int): String {
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            return sdf.format(calendar.time)
        }

        /**
         * İki tarih arasındaki gün farkı
         */
        fun getDaysSince(timestamp: Long): Int {
            val diff = System.currentTimeMillis() - timestamp
            return (diff / (1000 * 60 * 60 * 24)).toInt()
        }
    }
}