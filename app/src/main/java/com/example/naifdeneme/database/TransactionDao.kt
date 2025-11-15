package com.example.naifdeneme.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * TransactionDao - Finansal işlemler için veritabanı erişimi
 *
 * İşlemler:
 * - Tüm işlemleri getir (Flow ile canlı güncelleme)
 * - Gelir/gider ayrı ayrı getir
 * - Toplam gelir/gider hesapla
 * - Kategoriye göre filtrele
 * - Tarih aralığına göre filtrele
 */
@Dao
interface TransactionDao {

    /**
     * Tüm işlemleri getir (en yeni en üstte)
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /**
     * Widget için: Flow olmadan direkt liste
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    suspend fun getAllTransactionsForWidget(): List<TransactionEntity>

    /**
     * Sadece gelirleri getir
     */
    @Query("SELECT * FROM transactions WHERE type = 'INCOME' ORDER BY date DESC")
    fun getAllIncomes(): Flow<List<TransactionEntity>>

    /**
     * Sadece giderleri getir
     */
    @Query("SELECT * FROM transactions WHERE type = 'EXPENSE' ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<TransactionEntity>>

    /**
     * Toplam geliri hesapla
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'")
    suspend fun getTotalIncome(): Double

    /**
     * Toplam gideri hesapla
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'")
    suspend fun getTotalExpense(): Double

    /**
     * Bu ayki toplam gelir
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM transactions 
        WHERE type = 'INCOME' 
        AND date >= :monthStart 
        AND date < :monthEnd
    """)
    suspend fun getMonthlyIncome(monthStart: Long, monthEnd: Long): Double

    /**
     * Bu ayki toplam gider
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM transactions 
        WHERE type = 'EXPENSE' 
        AND date >= :monthStart 
        AND date < :monthEnd
    """)
    suspend fun getMonthlyExpense(monthStart: Long, monthEnd: Long): Double

    /**
     * Kategoriye göre toplam harcama
     */
    @Query("""
        SELECT category, SUM(amount) as total
        FROM transactions 
        WHERE type = 'EXPENSE'
        GROUP BY category
        ORDER BY total DESC
    """)
    suspend fun getExpensesByCategory(): List<CategoryTotal>

    /**
     * Belirli bir kategorideki işlemleri getir
     */
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    /**
     * Tarih aralığındaki işlemleri getir
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE date >= :startDate AND date < :endDate 
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    /**
     * ID'ye göre tek bir işlemi getir
     */
    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Int): TransactionEntity?

    /**
     * Yeni işlem ekle
     */
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    /**
     * İşlem güncelle
     */
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    /**
     * İşlem sil
     */
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    /**
     * Tüm işlemleri sil (test için)
     */
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

/**
 * Kategoriye göre toplam yardımcı sınıfı
 */
data class CategoryTotal(
    val category: String,
    val total: Double
)