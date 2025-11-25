package com.example.naifdeneme.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        HabitEntity::class,
        NotesEntity::class,
        TransactionEntity::class,
        PomodoroEntity::class,
        WaterEntryEntity::class,
        MedicineEntity::class
    ],
    version = 12, // 🔥 GÜNCELLENDİ
    exportSchema = false
)
@TypeConverters(HabitTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun notesDao(): NotesDao
    abstract fun transactionDao(): TransactionDao
    abstract fun pomodoroDao(): PomodoroDao
    abstract fun waterDao(): WaterDao
    abstract fun medicineDao(): MedicineDao

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
                    // 🔥 Şema değişikliğinde çökmemesi için veri tabanını sıfırlar
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}