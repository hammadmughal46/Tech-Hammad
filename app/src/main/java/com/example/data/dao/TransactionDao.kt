package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    fun getTransactionsForUserByType(userId: Long, type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR trxId LIKE '%' || :query || '%' OR type LIKE '%' || :query || '%' OR COALESCE(notes, '') LIKE '%' || :query || '%') ORDER BY timestamp DESC")
    fun searchTransactionsForUser(userId: Long, query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("UPDATE transactions SET status = :status WHERE trxId = :trxId")
    suspend fun updateTransactionStatus(trxId: String, status: String)

    @Query("SELECT * FROM transactions WHERE trxId = :trxId LIMIT 1")
    suspend fun getTransactionByTrxId(trxId: String): TransactionEntity?

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND type = 'DEPOSIT' AND status = 'APPROVED'")
    fun getUserTotalApprovedDeposits(userId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(ABS(amount)), 0.0) FROM transactions WHERE userId = :userId AND type = 'WITHDRAWAL' AND status IN ('PENDING', 'APPROVED', 'PAID')")
    fun getUserTotalWithdrawals(userId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE userId = :userId AND type = 'AD_REWARD' AND status = 'COMPLETED'")
    fun getUserTotalAdRewards(userId: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId AND type = 'AD_REWARD' AND status = 'COMPLETED'")
    fun getUserAdsCompletedCount(userId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId AND type = 'DEPOSIT'")
    fun getUserDepositsCount(userId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId AND type = 'WITHDRAWAL'")
    fun getUserWithdrawalsCount(userId: Long): Flow<Int>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'AD_REWARD' AND status = 'COMPLETED'")
    fun getTotalAdEarnings(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transactions WHERE type = 'AD_REWARD' AND status = 'COMPLETED'")
    fun getTotalAdsCompleted(): Flow<Int>
}
