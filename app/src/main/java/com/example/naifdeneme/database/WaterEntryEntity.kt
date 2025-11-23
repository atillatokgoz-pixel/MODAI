package com.example.naifdeneme.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Su İçme Kaydı Entity
 * Version 2: drinkType ve drinkIcon eklendi
 */
@Entity(tableName = "water_entries")
data class WaterEntryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val amount: Int, // ml cinsinden

    val drinkType: String = "water", // water, tea, coffee, juice, milk, other

    val drinkIcon: String? = null, // Emoji veya icon resource name (opsiyonel)

    val timestamp: Long = System.currentTimeMillis(),

    val note: String? = null
)

/**
 * İçecek Tipleri Enum
 */
enum class DrinkType(val id: String, val emoji: String, val nameResId: Int) {
    WATER("water", "💧", com.example.naifdeneme.R.string.drink_type_water),
    TEA("tea", "🍵", com.example.naifdeneme.R.string.drink_type_tea),
    COFFEE("coffee", "☕", com.example.naifdeneme.R.string.drink_type_coffee),
    JUICE("juice", "🧃", com.example.naifdeneme.R.string.drink_type_juice),
    MILK("milk", "🥛", com.example.naifdeneme.R.string.drink_type_milk),
    OTHER("other", "🥤", com.example.naifdeneme.R.string.drink_type_other);

    companion object {
        fun fromId(id: String): DrinkType {
            return values().find { it.id == id } ?: WATER
        }
    }
}