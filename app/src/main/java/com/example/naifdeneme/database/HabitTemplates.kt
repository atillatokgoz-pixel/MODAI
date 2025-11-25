package com.example.naifdeneme.database

import com.example.naifdeneme.model.HabitCategory
import com.example.naifdeneme.model.HabitTemplateData
import com.example.naifdeneme.model.HabitType

object HabitTemplates {
    val ALL = listOf(
        // --- SAĞLIK ---
        HabitTemplateData(
            id = "water",
            name = "Su İç",
            icon = "💧",
            color = 0xFF13ECEC,
            category = HabitCategory.HEALTH,
            type = HabitType.COUNTABLE,
            targetValue = 8,
            unit = "bardak",
            description = "Günde 8 bardak su için"
        ),
        HabitTemplateData(
            id = "sleep",
            name = "Erken Uyu",
            icon = "😴",
            color = 0xFF6B5CE7,
            category = HabitCategory.HEALTH,
            type = HabitType.SIMPLE,
            description = "Her gün erken yat"
        ),
        HabitTemplateData(
            id = "meditation",
            name = "Meditasyon",
            icon = "🧘",
            color = 0xFF9D50BB,
            category = HabitCategory.HEALTH,
            type = HabitType.TIMED,
            targetValue = 10,
            unit = "dakika",
            description = "Günde 10 dakika meditasyon"
        ),

        // --- EĞİTİM ---
        HabitTemplateData(
            id = "reading",
            name = "Kitap Oku",
            icon = "📚",
            color = 0xFF7F13EC,
            category = HabitCategory.EDUCATION,
            type = HabitType.COUNTABLE,
            targetValue = 30,
            unit = "sayfa",
            description = "Her gün 30 sayfa oku"
        ),
        HabitTemplateData(
            id = "language",
            name = "Dil Pratiği",
            icon = "🗣️",
            color = 0xFFE91E63,
            category = HabitCategory.EDUCATION,
            type = HabitType.TIMED,
            targetValue = 20,
            unit = "dakika",
            description = "Günde 20 dakika dil çalış"
        ),

        // --- FİTNESS ---
        HabitTemplateData(
            id = "workout",
            name = "Egzersiz",
            icon = "💪",
            color = 0xFFFF5722,
            category = HabitCategory.FITNESS,
            type = HabitType.TIMED,
            targetValue = 30,
            unit = "dakika",
            description = "30 dakika spor yap"
        ),
        HabitTemplateData(
            id = "walk",
            name = "Yürüyüş",
            icon = "🚶",
            color = 0xFF4CAF50,
            category = HabitCategory.FITNESS,
            type = HabitType.COUNTABLE,
            targetValue = 10000,
            unit = "adım",
            description = "10.000 adım at"
        ),

        // --- KİŞİSEL ---
        HabitTemplateData(
            id = "journal",
            name = "Günlük Yaz",
            icon = "📝",
            color = 0xFFFFC107,
            category = HabitCategory.PERSONAL,
            type = HabitType.SIMPLE,
            description = "Her gün günlük tut"
        ),
        HabitTemplateData(
            id = "gratitude",
            name = "Minnettarlık",
            icon = "🙏",
            color = 0xFFFF9800,
            category = HabitCategory.PERSONAL,
            type = HabitType.SIMPLE,
            description = "3 şey için minnettar ol"
        ),

        // --- İŞ ---
        HabitTemplateData(
            id = "no_social",
            name = "Sosyal Medya Yok",
            icon = "📵",
            color = 0xFF795548,
            category = HabitCategory.WORK,
            type = HabitType.SIMPLE,
            description = "İş saatlerinde sosyal medya kullanma"
        ),
        HabitTemplateData(
            id = "deep_work",
            name = "Derin Çalışma",
            icon = "🎯",
            color = 0xFF009688,
            category = HabitCategory.WORK,
            type = HabitType.TIMED,
            targetValue = 90,
            unit = "dakika",
            description = "90 dakika kesintisiz çalış"
        )
    )

    fun getByCategory(category: HabitCategory) = ALL.filter { it.category == category }

    fun getPopular() = ALL.take(6)
}