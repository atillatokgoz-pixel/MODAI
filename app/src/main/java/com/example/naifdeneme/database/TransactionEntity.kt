package com.example.naifdeneme.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * TransactionEntity - Gelir/Gider işlemleri için veritabanı tablosu
 *
 * Özellikler:
 * - id: Benzersiz kimlik
 * - type: İşlem tipi (INCOME/EXPENSE)
 * - amount: Tutar (Double)
 * - category: Kategori (Yemek, Ulaşım vb.)
 * - description: Açıklama (opsiyonel)
 * - date: İşlem tarihi (timestamp)
 * - createdAt: Kayıt zamanı
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // İşlem tipi: INCOME (gelir) veya EXPENSE (gider)
    val type: TransactionType,

    // Tutar (TL)
    val amount: Double,

    // Kategori
    val category: String,

    // Açıklama (opsiyonel)
    val description: String = "",

    // İşlem tarihi (kullanıcının seçtiği tarih)
    val date: Long = System.currentTimeMillis(),

    // Kayıt zamanı (sistem)
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * İşlem Tipi Enum
 */
enum class TransactionType {
    INCOME,   // Gelir
    EXPENSE   // Gider
}

/**
 * Önceden tanımlı kategoriler
 */
object TransactionCategories {
    // Gider kategorileri
    val EXPENSE_CATEGORIES = listOf(
        CategoryItem("🍔", "Yemek"),
        CategoryItem("🚗", "Ulaşım"),
        CategoryItem("🏠", "Kira"),
        CategoryItem("💡", "Faturalar"),
        CategoryItem("🛒", "Market"),
        CategoryItem("👕", "Giyim"),
        CategoryItem("🎬", "Eğlence"),
        CategoryItem("💊", "Sağlık"),
        CategoryItem("📚", "Eğitim"),
        CategoryItem("📱", "İletişim"),
        CategoryItem("🎁", "Hediye"),
        CategoryItem("❓", "Diğer")
    )

    // Gelir kategorileri
    val INCOME_CATEGORIES = listOf(
        CategoryItem("💼", "Maaş"),
        CategoryItem("💰", "Ek Gelir"),
        CategoryItem("🎁", "Hediye"),
        CategoryItem("📈", "Yatırım"),
        CategoryItem("💵", "Borç İadesi"),
        CategoryItem("❓", "Diğer")
    )

    fun getAllCategories(): List<CategoryItem> {
        return EXPENSE_CATEGORIES + INCOME_CATEGORIES
    }
}

/**
 * Kategori öğesi (emoji + isim)
 */
data class CategoryItem(
    val emoji: String,
    val name: String
)