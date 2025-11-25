package com.example.naifdeneme.model

import com.example.naifdeneme.database.HabitEntity

enum class HabitCategory {
    HEALTH,     // Sağlık
    EDUCATION,  // Eğitim
    WORK,       // İş
    PERSONAL,   // Kişisel
    FITNESS,    // Fitness
    FINANCE     // 🔥 EKLENDİ: Hub ekranı için gerekli
}

enum class HabitType {
    SIMPLE,     // Evet/Hayır
    COUNTABLE,  // Miktar (bardak, sayfa)
    TIMED       // Süre (dakika)
}

data class HabitTemplateData(
    val id: String,
    val name: String,
    val icon: String,
    val color: Long,
    val category: HabitCategory,
    val type: HabitType,
    val targetValue: Int? = null,
    val unit: String? = null,
    val description: String
)

// UI'da göstermek için helper extension
fun HabitCategory.displayName() = when(this) {
    HabitCategory.HEALTH -> "Sağlık"
    HabitCategory.EDUCATION -> "Eğitim"
    HabitCategory.WORK -> "İş"
    HabitCategory.PERSONAL -> "Kişisel"
    HabitCategory.FITNESS -> "Fitness"
    HabitCategory.FINANCE -> "Finans" // 🔥 EKLENDİ
}

// 🔥 YENİ: Template verisini Database Entity'sine çeviren yardımcı fonksiyon
// MainActivity içindeki kod kalabalığını azaltır ve standartlaştırır.
fun HabitTemplateData.toHabitEntity(): HabitEntity {
    return HabitEntity(
        name = this.name,
        description = this.description,
        icon = this.icon,
        color = this.color,
        category = this.category.name,
        // Model Enum'ını Database Enum'ına çeviriyoruz
        type = when (this.type) {
            HabitType.SIMPLE -> com.example.naifdeneme.database.HabitType.SIMPLE
            HabitType.COUNTABLE -> com.example.naifdeneme.database.HabitType.COUNTABLE
            HabitType.TIMED -> com.example.naifdeneme.database.HabitType.TIMED
        },
        targetValue = this.targetValue ?: 1,
        unit = this.unit ?: "adet",
        currentProgress = 0,
        frequency = "Daily",
        priority = 1
    )
}