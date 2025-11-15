package com.example.naifdeneme.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),

    val name: String,
    val dosage: String,
    val type: MedicineType,

    // Hatırlatıcı ayarları
    val reminderEnabled: Boolean = true,
    val reminderTimes: String = "08:00,20:00", // ✅ String olarak kaydet (virgülle ayır)
    val reminderDays: String = "1,2,3,4,5,6,7",

    // Takip
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val notes: String = "",

    val createdAt: Long = System.currentTimeMillis()
) {
    // ✅ Helper function: String'i List'e çevir
    fun getReminderTimesList(): List<String> {
        return if (reminderTimes.isBlank()) emptyList()
        else reminderTimes.split(",")
    }

    // ✅ Helper function: List'i String'e çevir
    companion object {
        fun fromReminderTimesList(times: List<String>): String {
            return times.joinToString(",")
        }
    }
}

enum class MedicineType {
    PILL, SYRUP, INJECTION, VITAMIN, OTHER
}