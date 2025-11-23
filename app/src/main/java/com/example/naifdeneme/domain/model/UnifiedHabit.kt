package com.example.naifdeneme.domain.model

enum class HabitSource {
    WATER,
    MEDICINE,
    POMODORO,
    HABIT,
    FINANCE,
    NOTES,
    SETTINGS
}

data class UnifiedHabit(
    val id: String,
    val title: String,              // Eski tip düz yazı (Yedek)
    val titleRes: Int? = null,      // 🔥 YENİ: Çoklu dil için ID (Örn: R.string.water)
    val subtitle: String,
    val icon: String,
    val color: Long,
    val progress: Float,
    val isCompleted: Boolean,
    val source: HabitSource,
    val originalId: Long? = null,
    val actionLabel: String? = null,      // Eski tip buton yazısı
    val actionLabelRes: Int? = null       // 🔥 YENİ: Çoklu dil için buton ID
)