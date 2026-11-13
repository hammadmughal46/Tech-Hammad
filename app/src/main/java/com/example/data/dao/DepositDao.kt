package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.DepositRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposits WHERE userId = :userId ORDER BY createdAt DESC")
    fun getDepositsForUser(userId: Long): Flow<List<DepositRequestEntity>>

    @Query("SELECT * FROM deposits ORDER BY createdAt DESC")
    fun getAllDeposits(): Flow<List<DepositRequestEntity>>

    @Query("SELECT * FROM deposits WHERE id = :id LIMIT 1")
    suspend fun getDepositById(id: Long): DepositRequestEntity?

    @Query("SELECT * FROM deposits WHERE trxId = :trxId LIMIT 1")
    suspend fun getDepositByTrxId(trxId: String): DepositRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositRequestEntity): Long

    @Update
    suspend fun updateDeposit(deposit: DepositRequestEntity)

    @Query("SELECT COUNT(*) FROM deposits WHERE status = 'PENDING'")
    fun getPendingDepositCount(): Flow<Int>

    @Query("SELECT SUM(amount) FROM deposits WHERE status = 'APPROVED'")
    fun getTotalApprovedDeposits(): Flow<Double?>
}
