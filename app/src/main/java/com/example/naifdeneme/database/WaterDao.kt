package com.example.naifdeneme.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Su Takip DAO
 * Version 2: DrinkType filtreleme ve Tarih (String) sorgusu eklendi
 */
@Dao
interface WaterDao {

    @Insert
    suspend fun insertEntry(entry: WaterEntryEntity)

    @Update
    suspend fun updateEntry(entry: WaterEntryEntity)

    @Query("SELECT * FROM water_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<WaterEntryEntity>>

    @Query("SELECT * FROM water_entries WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp DESC")
    fun getTodayEntries(startOfDay: Long, endOfDay: Long): Flow<List<WaterEntryEntity>>

    @Query("SELECT SUM(amount) FROM water_entries WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    fun getTodayTotalAmount(startOfDay: Long, endOfDay: Long): Flow<Int?>

    @Query("DELETE FROM water_entries WHERE id = :id")
    suspend fun deleteEntry(id: String)

    @Query("DELETE FROM water_entries")
    suspend fun deleteAllEntries()

    /**
     * İçecek tipine göre kayıtları getir
     */
    @Query("SELECT * FROM water_entries WHERE drinkType = :type ORDER BY timestamp DESC")
    fun getEntriesByDrinkType(type: String): Flow<List<WaterEntryEntity>>

    /**
     * Tarih aralığına göre kayıtları getir (history için)
     */
    @Query("SELECT * FROM water_entries WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getEntriesByDateRange(startDate: Long, endDate: Long): Flow<List<WaterEntryEntity>>

    /**
     * Belirli bir günün toplam miktarını getir
     */
    @Query("SELECT SUM(amount) FROM water_entries WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay AND drinkType = :type")
    fun getTotalAmountByType(startOfDay: Long, endOfDay: Long, type: String): Flow<Int?>

    /**
     * 🔥 EKLENDİ: String tarih formatına göre kayıtları getir
     * CategoryHubScreen içindeki WaterSummaryCard için gerekli.
     */
    @Query("SELECT * FROM water_entries WHERE date = :dateString ORDER BY timestamp DESC")
    fun getEntriesByDate(dateString: String): Flow<List<WaterEntryEntity>>
}