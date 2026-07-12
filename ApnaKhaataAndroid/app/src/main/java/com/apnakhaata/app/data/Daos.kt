package com.apnakhaata.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE confirmed = 0 ORDER BY date DESC")
    fun observePending(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions")
    suspend fun getAllOnce(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(t: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(list: List<TransactionEntity>): List<Long>

    @Query("UPDATE transactions SET purpose = :purpose, category = :category, confirmed = 1 WHERE id = :id")
    suspend fun confirm(id: Long, purpose: String, category: String)

    @Query("UPDATE transactions SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: Long, category: String)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM transactions WHERE date = :date AND amount = :amount AND merchant = :merchant AND type = :type")
    suspend fun countDuplicate(date: Long, amount: Double, merchant: String, type: String): Int
}

@Dao
interface CategoryRuleDao {

    @Query("SELECT * FROM category_rules ORDER BY id")
    fun observeAll(): Flow<List<CategoryRule>>

    @Query("SELECT * FROM category_rules ORDER BY id")
    suspend fun getAllOnce(): List<CategoryRule>

    @Query("SELECT COUNT(*) FROM category_rules")
    suspend fun count(): Int

    @Insert
    suspend fun insert(rule: CategoryRule)

    @Insert
    suspend fun insertAll(rules: List<CategoryRule>)

    @Query("DELETE FROM category_rules WHERE id = :id")
    suspend fun delete(id: Long)
}
