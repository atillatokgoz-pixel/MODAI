package com.example.naifdeneme.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    /**
     * Tüm notları getir (Flow ile canlı güncelleme)
     * DÜZELTME: timestamp yerine updatedAt kullanıldı
     */
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NotesEntity>>

    /**
     * Widget için: Flow olmadan direkt liste döndürür
     * DÜZELTME: timestamp yerine updatedAt kullanıldı
     */
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    suspend fun getAllNotesForWidget(): List<NotesEntity>

    /**
     * Toplam not sayısı
     */
    @Query("SELECT COUNT(*) FROM notes")
    fun getNoteCount(): Flow<Int>

    /**
     * ID'ye göre tek bir notu getir
     */
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): NotesEntity?

    @Insert
    suspend fun insertNote(note: NotesEntity)

    @Update
    suspend fun updateNote(note: NotesEntity)

    @Delete
    suspend fun deleteNote(note: NotesEntity)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}