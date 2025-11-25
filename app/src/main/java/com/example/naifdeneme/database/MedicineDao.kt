package com.example.naifdeneme.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    @Query("SELECT * FROM medicines ORDER BY time ASC")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    // Widget veya Hub için sync list
    @Query("SELECT * FROM medicines ORDER BY time ASC")
    suspend fun getAllMedicinesSync(): List<MedicineEntity>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: String): MedicineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity)

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)
}