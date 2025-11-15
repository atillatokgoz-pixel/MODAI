package com.example.naifdeneme.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Su İçme Kaydı Entity
 */
@Entity(tableName = "water_entries")
data class WaterEntryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val amount: Int, // ml cinsinden

    val timestamp: Long = System.currentTimeMillis(),

    val note: String? = null
)