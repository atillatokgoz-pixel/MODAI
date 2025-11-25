package com.example.naifdeneme.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val name: String,

    val dosage: String, // Örn: "1 Hap", "5ml"

    // 🔥 EKLENEN YENİ ALANLAR
    val time: String = "09:00", // Örn: "09:00", "22:30"

    val isTakenToday: Boolean = false, // Bugün alındı mı?

    val lastTakenDate: Long? = null // En son ne zaman alındı (timestamp)
)