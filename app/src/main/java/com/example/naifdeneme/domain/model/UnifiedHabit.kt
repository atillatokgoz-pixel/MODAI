package com.example.naifdeneme.domain.model

/**
 * Dashboard, kategori ekranı ve özet kartları için
 * Tüm alışkanlık türlerini (su, ilaç, pomodoro, normal alışkanlık)
 * tek bir modelde birleştiriyoruz.
 */
data class UnifiedHabit(
    val id: String,

    // --- Başlık ---
    val title: String,          // fallback plain text
    val titleRes: Int? = null,  // çoklu dil desteği

    // --- Alt açıklama ---
    val subtitle: String,

    // --- Görseller ---
    val icon: String,
    val color: Long,

    // --- İlerleme ---
    val progress: Float,
    val isCompleted: Boolean,

    // --- Navigation Kaynağı ---
    val source: HabitSource,
    val originalId: Long? = null,

    // --- Quick Action ---
    val actionLabel: String? = null,
    val actionLabelRes: Int? = null,

    // --- Yeni Eklenen Alanlar ---
    val category: String? = null,     // Sağlık / Eğitim / Finans vb.
    val targetValue: Int? = null,     // Kaç dk / kaç bardak vs.
    val currentValue: Int? = null,    // Güncel ilerleme
    val unit: String? = null          // "dk", "bardak", "sayfa" vs.
)
