package com.example.naifdeneme.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * NotesEntity - Notlar için veritabanı tablosu
 *
 * Her not şunları içerir:
 * - id: Benzersiz kimlik
 * - title: Not başlığı
 * - content: Not içeriği
 * - createdAt: Oluşturulma zamanı
 * - updatedAt: Son güncellenme zamanı
 */
@Entity(tableName = "notes")
data class NotesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val content: String,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)