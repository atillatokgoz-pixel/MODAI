package com.example.naifdeneme.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "water_entries")
data class WaterEntryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val amount: Int,
    val timestamp: Long,
    val note: String? = null,
    val drinkType: String = "water" // "water", "coffee", "tea", "juice"
)

// Drink Type Constants
object DrinkTypes {
    const val WATER = "water"
    const val COFFEE = "coffee"
    const val TEA = "tea"
    const val JUICE = "juice"
    const val SODA = "soda"
}