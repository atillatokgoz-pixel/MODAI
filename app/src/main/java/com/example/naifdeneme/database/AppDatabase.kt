package com.example.naifdeneme.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        HabitEntity::class,
        NotesEntity::class,
        TransactionEntity::class,
        PomodoroEntity::class,
        WaterEntryEntity::class,
        MedicineEntity::class  // ✅ YENİ EKLENDİ
    ],
    version = 7,  // ✅ VERSION 6 → 7 (MedicineEntity eklendiği için)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun notesDao(): NotesDao
    abstract fun transactionDao(): TransactionDao
    abstract fun pomodoroDao(): PomodoroDao
    abstract fun waterDao(): WaterDao
    abstract fun medicineDao(): MedicineDao  // ✅ YENİ EKLENDİ

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "modai_database"
                )
                    .fallbackToDestructiveMigration()  // ✅ Version artınca tablolar resetlenir
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}