package com.example.naifdeneme.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * NotesDao - Notlar için veritabanı işlemleri
 *
 * CRUD işlemleri:
 * - Create: insertNote
 * - Read: getAllNotes, getNoteById
 * - Update: updateNote
 * - Delete: deleteNote
 */
@Dao
interface NotesDao {

    /**
     * Tüm notları getir (Flow ile canlı güncelleme)
     * Compose ekranlarında kullanılır
     */
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NotesEntity>>

    /**
     * Widget için: Flow olmadan direkt liste döndürür
     */
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    suspend fun getAllNotesForWidget(): List<NotesEntity>

    /**
     * ID'ye göre tek bir notu getir
     */
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): NotesEntity?

    /**
     * Yeni not ekle
     */
    @Insert
    suspend fun insertNote(note: NotesEntity)

    /**
     * Notu güncelle
     */
    @Update
    suspend fun updateNote(note: NotesEntity)

    /**
     * Notu sil
     */
    @Delete
    suspend fun deleteNote(note: NotesEntity)

    /**
     * Tüm notları sil (test için)
     */
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}