package com.example.naifdeneme.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    @Insert
    suspend fun insertMedicine(medicine: MedicineEntity)

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE id = :medicineId")
    suspend fun getMedicineById(medicineId: String): MedicineEntity?

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    @Query("SELECT * FROM medicines WHERE reminderEnabled = 1")
    suspend fun getMedicinesWithReminders(): List<MedicineEntity>
}