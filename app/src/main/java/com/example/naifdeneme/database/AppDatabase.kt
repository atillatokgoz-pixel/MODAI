package com.example.naifdeneme.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 10, // ✅ VERSION 10 OLDU
    exportSchema = false
)
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

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE water_entries ADD COLUMN drinkType TEXT NOT NULL DEFAULT 'water'")
                database.execSQL("ALTER TABLE water_entries ADD COLUMN drinkIcon TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habits ADD COLUMN category TEXT NOT NULL DEFAULT 'OTHER'")
                database.execSQL("ALTER TABLE habits ADD COLUMN targetValue INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE habits ADD COLUMN unit TEXT NOT NULL DEFAULT ''")
            }
        }

        /**
         * 🔥 MIGRATION 9 → 10: Habit Type ve Progress Eklemesi
         */
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Type sütunu (Enum ordinal veya String olarak saklanabilir, burada String tercih ettik)
                database.execSQL("ALTER TABLE habits ADD COLUMN type TEXT NOT NULL DEFAULT 'SIMPLE'")
                // Progress sütunu
                database.execSQL("ALTER TABLE habits ADD COLUMN currentProgress INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "modai_database"
                )
                    .addMigrations(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10) // ✅ Eklendi
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}